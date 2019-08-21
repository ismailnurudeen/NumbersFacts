package xyz.ismailnurudeen.numberfacts

import android.annotation.SuppressLint
import android.arch.persistence.room.Room
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ShareCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.gson.Gson
import com.like.LikeButton
import com.like.OnLikeListener
import kotlinx.android.synthetic.main.layout_facts.*
import kotlinx.android.synthetic.main.layout_facts.view.*
import java.io.File
import java.net.URL
import java.util.*

class FactDisplayActivity : AppCompatActivity() {
    private lateinit var appUtils: AppUtils
    var type = 0
    private var colorIndex = 0

    companion object {
        var currentFact: Fact? = null
        var isFavourite = false
    }

    @SuppressLint("NewApi", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_facts)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        fact_layout_bottom_ad.loadAd(AdRequest.Builder()
                .addTestDevice("EE61FFC39B2F91254A201499649C0082").build())

        appUtils = AppUtils(this)
        val appDb = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "favourite-db"
        ).allowMainThreadQueries().build()
        setFactColors(true)

        val extras = intent.extras
        val factObjString = extras?.getString("EXTRA_FACT_OBJ")
        currentFact = if (!factObjString.isNullOrEmpty()) {
            isFavourite = extras?.getBoolean("IS_FAV_FACT", false)!!
            Gson().fromJson<Fact>(factObjString, Fact::class.java)
        } else {
            null
        }

        type = extras?.getInt(Constants.EXTRA_FACT_TYPE) ?: 0
        when (type) {
            Constants.TYPE_TRIVIA -> {
                supportActionBar?.title = "Trivia Facts"
                execFactTask("", type, true, currentFact)
                number_fact_form.visibility = View.VISIBLE
                facts_input_instruction.text = "Write any number to get fact:"
            }
            Constants.TYPE_DATE -> {
                supportActionBar?.title = "Date Facts"
                execFactTask("", type, true, currentFact)
                date_fact_form.visibility = View.VISIBLE
                facts_input_instruction.text = "Write any date to get fact:"
            }
            Constants.TYPE_YEAR -> {
                supportActionBar?.title = "Year Facts"
                execFactTask("", type, true, currentFact)
                year_fact_form.visibility = View.VISIBLE
                facts_input_instruction.text = "Write any year to get fact:"
            }
        }
        save_card_btn.setOnClickListener {
            if (currentFact != null) {
                val card = appUtils.getBitmapFromView(fact_card)
                if (appUtils.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, 105)) {
                    appUtils.saveBitmapToExternalStorage(card)
                }
            } else {
                Toast.makeText(this, "Can not save this fact to gallery", Toast.LENGTH_SHORT).show()
            }
        }
        add_to_fav_btn.setOnLikeListener(object : OnLikeListener {
            override fun liked(p0: LikeButton?) {
                if (currentFact != null) {
                    appDb.getFactDao().insert(currentFact!!)
                    Toast.makeText(this@FactDisplayActivity, "Added to favourite", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this@FactDisplayActivity, "Can not add fact to favourite", Toast.LENGTH_SHORT).show()
                    add_to_fav_btn.isLiked = false
                }
            }

            override fun unLiked(p0: LikeButton?) {
                appDb.getFactDao().delete(currentFact!!)
            }

        })
        share_fact_btn.setOnClickListener {
            val cardBitmap = appUtils.getBitmapFromView(fact_card)
            if (appUtils.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, 105)) {
                val imageFile = appUtils.getTempImageToShare(cardBitmap)
                val uri = FileProvider.getUriForFile(this@FactDisplayActivity, BuildConfig.APPLICATION_ID + ".provider", imageFile)
                ShareCompat.IntentBuilder
                        .from(this@FactDisplayActivity)
                        .setType("image/png")
                        .setText("Shared with NumberFacts app... Download on PlayStore: ${getString(R.string.app_playstore_link_template) + packageName}")
                        .setStream(uri)
                        .setChooserTitle("Share Fact with")
                        .startChooser()
                imageFile.deleteOnExit()
            }
        }
        bg_color_btn.setOnClickListener {
            setFactColors()
        }
    }

    override fun onResume() {
        super.onResume()
        val tempFile = File(Environment.getExternalStorageDirectory(), "temp_share_fact.png")
        if (tempFile.exists()) {
            tempFile.delete()
            // Toast.makeText(this@FactDisplayActivity, "Deleted!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setFactColors(random: Boolean = false) {
        val colorStrings = resources.getStringArray(R.array.colors)
        if (random) colorIndex = Random().nextInt(colorStrings.size - 1)
        if (colorIndex >= colorStrings.size - 1) colorIndex = 0

        val bgColor = Color.parseColor(colorStrings[colorIndex++])
        Log.i("BG-COLOR:INDEX", "${colorStrings[colorIndex]} : $colorIndex")
        val txtColor = if (appUtils.isWhiteText(bgColor)) {
            Color.WHITE
        } else {
            Color.DKGRAY
        }
        fact_card.setCardBackgroundColor(bgColor)
        fact_num_tv.setTextColor(txtColor)
        fact_text.setTextColor(txtColor)
    }


    private fun execFactTask(num: String, type: Int, isRandom: Boolean = false, favFact: Fact? = null) {
        if (appUtils.checkNetworkState() && favFact == null) {
            FactQueryTask(fact_card).execute(QueryUtils.getUrl(num, type, isRandom))
        } else if (favFact != null) {
            fact_card_loading.visibility = View.GONE
            fact_text.text = favFact!!.text
            fact_num_tv.text = if (type == Constants.TYPE_DATE) {
                val dateValue = favFact!!.text.split(" ")
                if (dateValue.isNotEmpty()) dateValue[0] + " " + dateValue[1]
                else favFact!!.number
            } else {
                favFact!!.number
            }
            add_to_fav_btn.isLiked = isFavourite
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show()
            fact_card_loading.visibility = View.GONE
            fact_text.text = "No Internet Connection!"
            fact_num_tv.text = ""
        }
    }

    fun onGetFactClicked(v: View) {
        add_to_fav_btn.isLiked = false
        val number = when (type) {
            Constants.TYPE_TRIVIA -> {
                number_fact_input.text.toString()
            }
            Constants.TYPE_DATE -> {
                date_month.text.toString() + "/" + date_day.text.toString()
            }
            Constants.TYPE_YEAR -> {
                year_fact_input.text.toString()
            }
            else -> ""
        }
        if (number.isEmpty() || number == "/") {
            Toast.makeText(this, "Empty Value...", Toast.LENGTH_SHORT).show()
            return
        }
        execFactTask(number, type)
    }

    class FactQueryTask(val factCard: CardView) : AsyncTask<URL, Void, Fact>() {
        override fun onPreExecute() {
            super.onPreExecute()
            factCard.fact_card_loading.startAnim()
            factCard.fact_card_loading.setViewColor(ContextCompat.getColor(factCard.context, R.color.colorAccent))
            factCard.fact_card_loading.visibility = View.VISIBLE
            factCard.fact_text.text = ""
            factCard.fact_num_tv.text = ""
        }

        override fun doInBackground(vararg params: URL?): Fact {
            return QueryUtils.getApiResult(params[0]!!)
        }

        override fun onPostExecute(result: Fact?) {
            super.onPostExecute(result)
            factCard.fact_card_loading.stopAnim()
            factCard.fact_card_loading.visibility = View.GONE
            if (!result?.found!! && result.text.isEmpty()) {
                factCard.fact_text.text = "No result found..."
                return
            }
            factCard.fact_text.text = result.text
            factCard.fact_num_tv.text = if (result.type.equals("date", true)) {
                val dateValue = result.text.split(" ")
                if (dateValue.isNotEmpty()) dateValue[0] + " " + dateValue[1]
                else result.number
            } else {
                result.number
            }
            currentFact = result
        }
    }
}
