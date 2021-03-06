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
package soup.movie.theatermap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import soup.movie.model.TheaterAreaGroup
import soup.movie.model.repository.MoopRepository
import timber.log.Timber

class TheaterMapViewModel(
    private val repository: MoopRepository
) : ViewModel() {

    private val _uiModel = MutableLiveData<TheaterMapUiModel>()
    val uiModel: LiveData<TheaterMapUiModel>
        get() = _uiModel

    init {
        viewModelScope.launch {
            _uiModel.value = loadUiModel()
        }
    }

    private suspend fun loadUiModel(): TheaterMapUiModel {
        return withContext(Dispatchers.IO) {
            try {
                TheaterMapUiModel(repository.getCodeList().toTheaterList())
            } catch (t: Throwable) {
                Timber.w(t)
                TheaterMapUiModel(emptyList())
            }
        }
    }

    private fun TheaterAreaGroup.toTheaterList(): List<TheaterMarkerUiModel> {
        return cgv.flatMap { group ->
            group.theaterList.map {
                CgvMarkerUiModel(
                    areaCode = group.area.code,
                    code = it.code,
                    name = "CGV ${it.name}",
                    lat = it.lat,
                    lng = it.lng
                )
            }
        } + lotte.flatMap { group ->
            group.theaterList.map {
                LotteCinemaMarkerUiModel(
                    areaCode = group.area.code,
                    code = it.code,
                    name = "롯데시네마 ${it.name}",
                    lat = it.lat,
                    lng = it.lng
                )
            }
        } + megabox.flatMap { group ->
            group.theaterList.map {
                MegaboxMarkerUiModel(
                    areaCode = group.area.code,
                    code = it.code,
                    name = "메가박스 ${it.name}",
                    lat = it.lat,
                    lng = it.lng
                )
            }
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _uiModel.value = loadUiModel()
        }
    }
}
