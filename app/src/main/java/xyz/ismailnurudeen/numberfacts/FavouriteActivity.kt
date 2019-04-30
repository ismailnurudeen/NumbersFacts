package xyz.ismailnurudeen.numberfacts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.favourite_itemview.view.*
import kotlinx.android.synthetic.main.layout_favourite.*
import xyz.ismailnurudeen.numberfacts.utilities.MiniDB

class FavouriteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_favourite)
        val miniDb = MiniDB.open(this, "favourites_db")
        val favFacts = miniDb.readList("facts_db", ArrayList<Fact>() as List<Any>?) as ArrayList<Fact>

        if (favFacts.isEmpty()) {
            no_fav_view.visibility = View.VISIBLE
            return
        }
//        favFacts.add(Fact("0 is the number of infinity", "0", true, "trivia"))
//        favFacts.add(Fact("10 is a the square root of 100", "10", true, "math"))
//        favFacts.add(Fact("101 looks binary", "101", true, "trivia"))
//        favFacts.add(Fact("1000 is the same as 10 to the positive power of 3", "1000", true, "math"))
//        favFacts.add(Fact("0 is the number of infinity", "0", true, "trivia"))
//        favFacts.add(Fact("10 is a the square root of 100", "10", true, "math"))
//        favFacts.add(Fact("101 looks binary", "101", true, "trivia"))
//        favFacts.add(Fact("1000 is the same as 10 to the positive power of 3", "1000", true, "math"))
//        favFacts.add(Fact("0 is the number of infinity", "0", true, "trivia"))
//        favFacts.add(Fact("10 is a the square root of 100", "10", true, "math"))
//        favFacts.add(Fact("101 looks binary", "101", true, "trivia"))
//        favFacts.add(Fact("1000 is the same as 10 to the positive power of 3", "1000", true, "math"))
//        favFacts.add(Fact("0 is the number of infinity", "0", true, "trivia"))
//        favFacts.add(Fact("10 is a the square root of 100", "10", true, "math"))
//        favFacts.add(Fact("101 looks binary", "101", true, "trivia"))
//        favFacts.add(Fact("1000 is the same as 10 to the positive power of 3", "1000", true, "math"))
        favourite_rv.adapter = FavouriteAdapter(this, favFacts)
    }

    class FavouriteAdapter(private val context: Context, private val factList: ArrayList<Fact>) : RecyclerView.Adapter<FavouriteAdapter.ViewHolder>() {
        override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
            holder.bind(factList[pos])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(context, LayoutInflater.from(context).inflate(R.layout.favourite_itemview, parent, false))


        override fun getItemCount(): Int = factList.size

        class ViewHolder(private val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(fact: Fact) {
                itemView.fav_item_view_number.text = fact.number
                itemView.fav_item_view_fact.text = fact.text
                itemView.setOnClickListener {
                    val intent = Intent(context, FactDisplayActivity::class.java)
                    val type = when (fact.type) {
                        "trivia" -> Constants.TYPE_TRIVIA
                        "date" -> Constants.TYPE_DATE
                        "year" -> Constants.TYPE_YEAR
                        else -> -1
                    }
                    intent.putExtra(Constants.EXTRA_FACT_TYPE, type)
                    intent.putExtra("EXTRA_FACT_OBJ", Gson().toJson(fact))
                    context.startActivity(intent)
                }
            }
        }
    }
}