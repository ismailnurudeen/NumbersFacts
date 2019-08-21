package xyz.ismailnurudeen.numberfacts

import android.annotation.SuppressLint
import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.favourite_itemview.view.*
import kotlinx.android.synthetic.main.layout_favourite.*

class FavouriteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_favourite)
        val appDb = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "favourite-db"
        ).build()

        val roomAsync = @SuppressLint("StaticFieldLeak")
        object : AsyncTask<Void, Void, ArrayList<Fact>>() {
            override fun doInBackground(vararg params: Void?): ArrayList<Fact> {
                return appDb.getFactDao().allFact() as ArrayList<Fact>
            }

            override fun onPostExecute(favouriteList: ArrayList<Fact>?) {
                super.onPostExecute(favouriteList)

                if (favouriteList!!.isEmpty()) {
                    no_fav_view.visibility = View.VISIBLE
                    return
                }
                val onItemClick = object : FavouriteAdapter.OnItemClickListener {
                    override fun onItemClick(pos: Int, fact: Fact) {
                        val intent = Intent(this@FavouriteActivity, FactDisplayActivity::class.java)
                        val type = when (fact.type) {
                            "trivia" -> Constants.TYPE_TRIVIA
                            "date" -> Constants.TYPE_DATE
                            "year" -> Constants.TYPE_YEAR
                            else -> -1
                        }
                        intent.putExtra(Constants.EXTRA_FACT_TYPE, type)
                        intent.putExtra("EXTRA_FACT_OBJ", Gson().toJson(fact))
                        intent.putExtra("IS_FAV_FACT", true)
                        startActivity(intent)
                    }

                }
                val onItemLongClick = object : FavouriteAdapter.OnItemLongClickListener {
                    override fun onItemLongClick(pos: Int, fact: Fact) {
                        Thread(Runnable {
                            appDb.getFactDao().delete(fact)
                        }).start()

                        favouriteList.removeAt(pos)
                        favourite_rv.adapter?.notifyDataSetChanged()
                        if (favouriteList.isEmpty()) {
                            no_fav_view.visibility = View.VISIBLE
                        }
                        Toast.makeText(this@FavouriteActivity, "Fact removed from favourite!", Toast.LENGTH_SHORT).show()
                    }

                }
                favourite_rv.adapter = FavouriteAdapter(this@FavouriteActivity, favouriteList, onItemClick, onItemLongClick)
            }
        }
        roomAsync.execute()
    }

    class FavouriteAdapter(private val context: Context, private val factList: ArrayList<Fact>, val onItemClick: OnItemClickListener, val onItemLongClick: OnItemLongClickListener) : RecyclerView.Adapter<FavouriteAdapter.ViewHolder>() {
        override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
            holder.bind(factList[pos])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(context).inflate(R.layout.favourite_itemview, parent, false), onItemClick, onItemLongClick)


        override fun getItemCount(): Int = factList.size

        class ViewHolder(itemView: View, val onItemClick: OnItemClickListener, val onItemLongClick: OnItemLongClickListener) : RecyclerView.ViewHolder(itemView) {
            fun bind(fact: Fact) {
                itemView.fav_item_view_number.text = fact.number
                itemView.fav_item_view_fact.text = fact.text
                itemView.setOnClickListener {
                    onItemClick.onItemClick(adapterPosition, fact)
                }
                itemView.setOnLongClickListener {
                    onItemLongClick.onItemLongClick(adapterPosition, fact)
                    true
                }
            }
        }

        interface OnItemClickListener {
            fun onItemClick(pos: Int, fact: Fact)
        }

        interface OnItemLongClickListener {
            fun onItemLongClick(pos: Int, fact: Fact)
        }
    }
}