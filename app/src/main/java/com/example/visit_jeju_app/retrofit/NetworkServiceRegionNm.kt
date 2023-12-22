package com.example.visit_jeju_app.retrofit

import com.example.visit_jeju_app.accommodation.model.AccomList
import com.example.visit_jeju_app.festival.model.FesList
import com.example.visit_jeju_app.restaurant.model.ResList
import com.example.visit_jeju_app.shopping.model.ShopList
import com.example.visit_jeju_app.tour.model.TourList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NetworkServiceRegionNm {
    @GET("tour/tourList/{itemsRegion2CdValue}")
    fun getList(
        @Path("itemsRegion2CdValue") itemsRegion2CdValue: Int
    ): Call<List<TourList>>

    @GET("res/resList/{itemsRegion2CdValue}")
    fun getResList(
        @Path("itemsRegion2CdValue") itemsRegion2CdValue: Int
    ): Call<List<ResList>>

    @GET("accom/accomList/{itemsRegion2CdValue}")
    fun getAccomList(
        @Path("itemsRegion2CdValue") itemsRegion2CdValue: Int
    ): Call<List<AccomList>>

    @GET("shop/shopList/{itemsRegion2CdValue}")
    fun getShopList(
        @Path("itemsRegion2CdValue") itemsRegion2CdValue: Int
    ): Call<List<ShopList>>

    @GET("fes/fesList/{itemsRegion2CdValue}")
    fun getFesList(
        @Path("itemsRegion2CdValue") itemsRegion2CdValue: Int
    ): Call<List<FesList>>


    @GET("tour/tourAllList")
    fun GetTourList(): Call<List<TourList>>

    @GET("accom/accomAllList")
    fun GetAccomList(): Call<List<AccomList>>

    @GET("res/resAllList")
    fun GetResList(): Call<List<ResList>>

    @GET("shop/shopAllList")
    fun GetShopList(): Call<List<ShopList>>

    @GET("fes/fesAllList")
    fun GetFesList(): Call<List<FesList>>
//
//    @GET("fes/fesAllList")
//    fun GetFesList(): Call<List<FesList>>


    @GET("tour/tourList/tourByGPS")
    fun getTourGPS(
        @Query("lat") lat : Double?,
        @Query("lnt") lnt : Double?,
        @Query("page") page : Int?
    ): Call<MutableList<TourList>>

//    http://10.100.104.32:8083/tour/tourList/tourByGPS/?lat=33.4&lnt=126.2?page=1

    @GET("accom/accomList/accomByGPS")
    fun getAccomGPS(
        @Query("lat") lat : Double?,
        @Query("lnt") lnt : Double?,
        @Query("page") page : Int?
    ): Call<MutableList<AccomList>>

    @GET("res/resList/resByGPS")
    fun getResGPS(
        @Query("lat") lat : Double?,
        @Query("lnt") lnt : Double?,
        @Query("page") page : Int?
    ): Call<MutableList<ResList>>

    @GET("fes/fesList/fesByGPS")
    fun getFesGPS(
        @Query("lat") lat : Double?,
        @Query("lnt") lnt : Double?,
        @Query("page") page : Int?
    ): Call<MutableList<FesList>>

    @GET("shop/shopList/shopByGPS")
    fun getShopGPS(
        @Query("lat") lat : Double?,
        @Query("lnt") lnt : Double?,
        @Query("page") page : Int?
    ): Call<MutableList<ShopList>>





}





