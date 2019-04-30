package xyz.ismailnurudeen.numberfacts

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.net.URL

class MainActivity : AppCompatActivity() {
    lateinit var appUtils: AppUtils
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appUtils = AppUtils(this)
        getRandomFact()
        random_fact_shuffle_btn.setOnClickListener {
            getRandomFact()
        }
    }

    private fun getRandomFact() {
        if (appUtils.checkPermission(android.Manifest.permission.INTERNET, 101)) {
            if (appUtils.checkNetworkState()) {
                QueryAsyncTask(home_fact_card).execute(QueryUtils.getUrl("random", -1))
            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show()
                home_fact_text.text = "No Internet Connection!\nPlease Turn on your internet connection..."
                home_fact_header.text = ""
            }
        }
    }

    fun onHomeBtnClick(v: View) {
        if (v.id != R.id.favourite_fact_btn) {
            val intent = Intent(this, FactDisplayActivity::class.java)
            val type = when (v.id) {
                R.id.numbers_fact_btn -> Constants.TYPE_TRIVIA
                R.id.dates_fact_btn -> Constants.TYPE_DATE
                R.id.year_fact_btn -> Constants.TYPE_YEAR
                else -> -1
            }
            intent.putExtra(Constants.EXTRA_FACT_TYPE, type)
            startActivity(intent)
        } else {
            startActivity(Intent(this, FavouriteActivity::class.java))
        }
    }

    class QueryAsyncTask(@SuppressLint("StaticFieldLeak") private val homeFactCard: CardView) : AsyncTask<URL, Void, Fact>() {
        override fun onPreExecute() {
            super.onPreExecute()
            homeFactCard.home_fact_card_loading.startAnim()
            homeFactCard.home_fact_card_loading.setViewColor(ContextCompat.getColor(homeFactCard.context, R.color.colorAccent))
            homeFactCard.home_fact_card_loading.visibility = View.VISIBLE
            homeFactCard.home_fact_header.text = ""
            homeFactCard.home_fact_text.text = ""
        }

        override fun doInBackground(vararg params: URL?): Fact {
            return QueryUtils.getApiResult(params[0]!!)
        }

        override fun onPostExecute(result: Fact?) {
            super.onPostExecute(result)
            homeFactCard.home_fact_card_loading.stopAnim()
            homeFactCard.home_fact_card_loading.visibility = View.GONE
            if (!result?.found!! && result.text.isEmpty()) {
                homeFactCard.home_fact_text.text = "No result found..."
                return
            }
            homeFactCard.home_fact_text.text = result.text
            homeFactCard.home_fact_header.text = if (result.type.equals("date", true)) {
                val dateValue = result.text.split(" ")
                if (dateValue.isNotEmpty()) dateValue[0] + " " + dateValue[1]
                else result.number
            } else {
                result.number
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_info -> startActivity(Intent(this, AboutActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getRandomFact()
            }
        }
    }
}
