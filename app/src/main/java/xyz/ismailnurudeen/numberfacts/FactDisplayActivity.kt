package xyz.ismailnurudeen.numberfacts

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.like.LikeButton
import com.like.OnLikeListener
import kotlinx.android.synthetic.main.layout_facts.*
import kotlinx.android.synthetic.main.layout_facts.view.*
import xyz.ismailnurudeen.numberfacts.utilities.MiniDB
import java.net.URL

class FactDisplayActivity : AppCompatActivity() {
    private lateinit var appUtils: AppUtils
    var type = 0

    companion object {
        var currentFact: Fact? = null
    }

    @SuppressLint("NewApi", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_facts)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        appUtils = AppUtils(this)
        val miniDb = MiniDB.open(this, "favourites_db")
        if (miniDb.readList("facts_db", ArrayList<Fact>() as List<Any>?).isEmpty()) {
            miniDb.insertList("facts_db", ArrayList<Fact>() as List<Any>?)
        }
        val extras = intent.extras
        val factObjString = extras?.getString("EXTRA_FACT_OBJ")
        val factObj = if (factObjString.isNullOrEmpty()) {
            Gson().fromJson<Fact>(factObjString, Fact::class.java)
        } else {
            null
        }
        type = extras?.getInt(Constants.EXTRA_FACT_TYPE) ?: 0
        when (type) {
            Constants.TYPE_TRIVIA -> {
                supportActionBar?.title = "Trivia Facts"
                execFactTask("", type, true, factObj)
                number_fact_form.visibility = View.VISIBLE
                facts_input_instruction.text = "Write any number to get fact:"
            }
            Constants.TYPE_DATE -> {
                supportActionBar?.title = "Date Facts"
                execFactTask("", type, true, factObj)
                date_fact_form.visibility = View.VISIBLE
                facts_input_instruction.text = "Write any date to get fact:"
            }
            Constants.TYPE_YEAR -> {
                supportActionBar?.title = "Year Facts"
                execFactTask("", type, true, factObj)
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
                val factsList = miniDb.readList("facts_db", ArrayList<Fact>() as List<Any>?) as ArrayList<Fact>
                factsList.add(Fact("0 is the number of infinity", "0", true, "trivia"))
                factsList.add(Fact("10 is a the square root of 100", "10", true, "math"))
                factsList.add(Fact("101 looks binary", "101", true, "trivia"))
                factsList.add(Fact("1000 is the same as 10 to the positive power of 3", "1000", true, "math"))
                factsList.add(Fact("0 is the number of infinity", "0", true, "trivia"))
                factsList.add(Fact("10 is a the square root of 100", "10", true, "math"))
                factsList.add(Fact("101 looks binary", "101", true, "trivia"))

                miniDb.insertList("facts_list", factsList as List<Any>?)
                if (currentFact != null) {
                    factsList.add(currentFact!!)
                    miniDb.insertList("facts_list", factsList as List<Any>?)
                    Toast.makeText(this@FactDisplayActivity, "Added to favourite", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@FactDisplayActivity, "Can not add fact to favourite", Toast.LENGTH_SHORT).show()
                    add_to_fav_btn.isLiked = false
                }
            }

            override fun unLiked(p0: LikeButton?) {
                val factsList = miniDb.readList("facts_db", ArrayList<Fact>() as List<Any>?) as ArrayList<Fact>
                if (currentFact != null && factsList.isNotEmpty()) {
                    factsList.removeAt(factsList.indexOf(currentFact!!))
                    miniDb.insertList("facts_list", factsList as List<Any>?)
                    add_to_fav_btn.isLiked = false
                    Toast.makeText(this@FactDisplayActivity, "Removed from favourite", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@FactDisplayActivity, "Removed from favourite", Toast.LENGTH_SHORT).show()
                    add_to_fav_btn.isLiked = false
                }
            }

        })
        share_fact_btn.setOnClickListener {

        }
        text_color_btn.setOnClickListener {
            setColorFromPicker(it.id)
        }
        bg_color_btn.setOnClickListener {
            setColorFromPicker(it.id)
        }
    }

    private fun setColorFromPicker(id: Int) {
//        val specDial = SpectrumDialog()
//        specDial.setOnColorSelectedListener { _, color ->
//            if (id == text_color_btn.id) {
//                fact_text.setTextColor(color)
//                fact_num_tv.setTextColor(color)
//                text_color_btn.setBackgroundColor(color)
//            } else {
//                fact_card.setCardBackgroundColor(color)
//                bg_color_btn.setBackgroundColor(color)
//                if (appUtils.isWhiteText(color)) {
//                    Toast.makeText(baseContext, "White Text", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(baseContext, "Dark Text", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//        specDial.show(supportFragmentManager, "Pick a Color")
    }


    private fun execFactTask(num: String, type: Int, isRandom: Boolean = false, factObj: Fact? = null) {
        if (appUtils.checkNetworkState() && factObj == null) {
            FactQueryTask(fact_card).execute(QueryUtils.getUrl(num, type, isRandom))
        } else if (factObj != null) {
            fact_card_loading.visibility = View.GONE
            fact_text.text = factObj.text
            fact_num_tv.text = if (type == Constants.TYPE_DATE) {
                val dateValue = factObj.text.split(" ")
                if (dateValue.isNotEmpty()) dateValue[0] + " " + dateValue[1]
                else factObj.number
            } else {
                factObj.number
            }
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show()
            fact_card_loading.visibility = View.GONE
            fact_text.text = "No Internet Connection!"
            fact_num_tv.text = ""
        }
    }

    fun onGetFactClicked(v: View) {
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
