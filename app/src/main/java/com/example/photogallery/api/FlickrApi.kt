package com.example.photogallery.api

import retrofit2.http.GET
import retrofit2.http.Query


private const val API_KEY = "36dc6329a8e1fd8858835610ce066783"

interface FlickrApi {

    @GET("services/rest/?method=flickr.interestingness.getList")
    suspend fun fetchPhotos(): FlickrResponse

    @GET("services/rest?method=flickr.photos.search")
    suspend fun searchPhotos(@Query("text") query: String): FlickrResponse
}