package com.example.pleasegod.view.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.pleasegod.R
import com.example.pleasegod.model.entity.Restroom
import com.example.pleasegod.observer.DefaultObserver
import com.example.pleasegod.view.RestroomMapActivity
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.item_restroom.view.*
import java.util.concurrent.TimeUnit

/**
 * Created by hclee on 2019-06-30.
 */

class RestroomListAdapter(
    private val mActivity: Activity,
    private val mGlideRequestManager: RequestManager,
    private val mRestroomList: MutableList<Restroom>
) : RecyclerView.Adapter<RestroomListAdapter.RestroomViewHolder>(), Filterable {
    companion object {
        val TAG: String = RestroomListAdapter::class.java.simpleName
        val INTENT_KEY: String = "selected_restroom"
    }

    private var mTotalRestroomList: MutableList<Restroom> = mutableListOf()
    private val mCompositeDisposable: CompositeDisposable = CompositeDisposable()

    fun copyTotalRestroom() {
        mTotalRestroomList.clear()
        mTotalRestroomList.addAll(mRestroomList)
    }

    fun restoreTotalRestroomData() {
        mRestroomList.clear()
        mRestroomList.addAll(mTotalRestroomList)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestroomViewHolder {
        val view: View =
            LayoutInflater.from(mActivity.applicationContext).inflate(R.layout.item_restroom, parent, false)

        return RestroomViewHolder(view)
    }

    override fun getItemCount(): Int = mRestroomList.size

    override fun onBindViewHolder(holder: RestroomViewHolder, position: Int) {
        mRestroomList[position].let { restroom ->
            mGlideRequestManager
                .load(R.drawable.pray)
                .into(holder.mPrayImageView)
            holder.mRestroomNameTextView.text = restroom.pbctlt_plc_nm
            holder.mRoadNameAddressTextView.text = restroom.refine_roadnm_addr
            holder.mRegularTimeTextView.text = restroom.open_tm_info

            mCompositeDisposable.add(
                holder.itemView.clicks()
                    .debounce(200, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DefaultObserver<Unit>() {
                        override fun onNext(t: Unit) {
                            val intent: Intent = Intent(mActivity, RestroomMapActivity::class.java).apply {
                                putExtra(INTENT_KEY, restroom.refine_roadnm_addr)
                            }

                            mActivity.startActivity(intent)
                            mActivity.overridePendingTransition(
                                R.anim.animation_slide_from_right,
                                R.anim.animation_slide_to_left
                            )
                        }
                    })
            )
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val query: String = charSequence.toString()

                return FilterResults().apply {
                    if (query.isEmpty()) {
                        count = mTotalRestroomList.size
                        values = mTotalRestroomList
                    } else {
                        mTotalRestroomList.filter {
                            (it.pbctlt_plc_nm != null && it.pbctlt_plc_nm.contains(query)) ||
                                    (it.refine_roadnm_addr != null && it.refine_roadnm_addr.contains(query))
                        }.let {
                            count = it.size
                            values = it
                        }
                    }
                }
            }

            override fun publishResults(charSequence: CharSequence?, results: FilterResults?) {
                results?.values?.let {
                    mRestroomList.clear()
                    mRestroomList.addAll(it as MutableList<Restroom>)

                    notifyDataSetChanged()
                }
            }
        }
    }

    fun clearDisposable() {
        mCompositeDisposable.clear()
    }

    class RestroomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mPrayImageView: ImageView = itemView.iv_pray
        val mRestroomNameTextView: TextView = itemView.tv_restroom_name
        val mRoadNameAddressTextView: TextView = itemView.tv_road_name_address
        val mRegularTimeTextView: TextView = itemView.tv_regular_time
    }
}