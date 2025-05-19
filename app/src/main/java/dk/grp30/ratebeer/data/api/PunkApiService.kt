package dk.grp30.ratebeer.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class Beer(
    val id: Int,
    val name: String,
    val tagline: String,
    val first_brewed: String,
    val description: String,
    val image: String,
    val abv: Double,
    val ibu: Double?,
    val ebc: Double?,
) {
    fun getFormattedImageUrl(): String {
        return "https://punkapi.online/v3/images/${String.format("%03d", id)}.png"
    }
}

interface PunkApi {
    @GET("beers")
    suspend fun searchBeersByName(
        @Query("beer_name") beerName: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): List<Beer>
    
    @GET("beers/{id}")
    suspend fun getBeerById(@Path("id") beerId: Int): Beer
}

object PunkApiService {
    private const val BASE_URL = "https://punkapi.online/v3/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api: PunkApi = retrofit.create(PunkApi::class.java)

    suspend fun searchBeersByName(query: String, page: Int = 1, perPage: Int = 30): Result<List<Beer>> {
        return try {
            val beers = api.searchBeersByName(query, page, perPage)
            Result.success(beers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBeerById(beerId: Int): Result<Beer> {
        return try {
            val response = api.getBeerById(beerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}