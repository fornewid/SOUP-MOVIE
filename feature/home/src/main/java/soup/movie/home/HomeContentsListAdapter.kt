/*
 * Copyright 2021 SOUP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package soup.movie.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ext.IdBasedDiffCallback
import soup.movie.ext.asyncText
import soup.movie.ext.consume
import soup.movie.ext.getAgeBackground
import soup.movie.ext.getDDayLabel
import soup.movie.ext.isBest
import soup.movie.ext.isDDay
import soup.movie.ext.isNew
import soup.movie.ext.loadAsync
import soup.movie.ext.showToast
import soup.movie.home.databinding.HomeItemMovieBinding
import soup.movie.model.Movie
import soup.movie.util.setOnDebounceClickListener

class HomeContentsListAdapter(
    context: Context,
    diffCallback: DiffUtil.ItemCallback<Movie> = IdBasedDiffCallback { it.id },
    private val listener: (Movie, Array<Pair<View, String>>) -> Unit
) : ListAdapter<Movie, HomeContentsListAdapter.MovieViewHolder>(diffCallback) {

    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = HomeItemMovieBinding.inflate(layoutInflater, parent, false)
        return MovieViewHolder(binding).apply {
            itemView.setOnDebounceClickListener(delay = 150L) {
                val index = bindingAdapterPosition
                if (index in 0..itemCount) {
                    val movie: Movie = getItem(index)
                    listener(movie, createSharedElements(movie))
                }
            }
            itemView.setOnLongClickListener {
                consume {
                    val index = bindingAdapterPosition
                    if (index in 0..itemCount) {
                        val movie: Movie = getItem(index)
                        it?.context?.showToast(movie.title)
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int) = R.layout.home_item_movie

    private fun MovieViewHolder.createSharedElements(movie: Movie): Array<Pair<View, String>> {
        itemView.run {
            val sharedElements = mutableListOf(
                backgroundView to R.string.transition_background,
                posterView to R.string.transition_poster,
                ageBgView to R.string.transition_age_bg
            )
            if (movie.isNew()) {
                sharedElements.add(newView to R.string.transition_new)
            }
            if (movie.isBest()) {
                sharedElements.add(bestView to R.string.transition_best)
            }
            if (movie.isDDay()) {
                sharedElements.add(dDayView to R.string.transition_d_day)
            }
            return sharedElements.toTypedArray()
        }
    }

    private infix fun View.to(@StringRes tagId: Int): Pair<View, String> {
        return Pair(this, context.getString(tagId))
    }

    class MovieViewHolder(private val binding: HomeItemMovieBinding) : RecyclerView.ViewHolder(binding.root) {

        val backgroundView = binding.backgroundView
        val posterView = binding.posterView
        val ageBgView = binding.ageBgView.root
        val newView = binding.newView.root
        val bestView = binding.bestView.root
        val dDayView = binding.dDayView.root

        fun bind(item: Movie) {
            binding.container.tag = item.id
            posterView.loadAsync(item.posterUrl, R.drawable.bg_on_surface_dim)
            posterView.contentDescription = item.title
            ageBgView.setBackgroundResource(item.getAgeBackground())
            newView.isVisible = item.isNew()
            bestView.isVisible = item.isBest()
            dDayView.isVisible = item.isDDay()
            dDayView.asyncText(item.getDDayLabel())
        }
    }
}
