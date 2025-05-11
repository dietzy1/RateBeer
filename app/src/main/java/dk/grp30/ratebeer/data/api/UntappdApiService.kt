package dk.grp30.ratebeer.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// These models would be expanded in a real app
data class Beer(
    val id: String,
    val name: String,
    val brewery: String,
    val style: String,
    val abv: Double,
    val rating: Double,
    val imageUrl: String
)

data class SearchResponse(
    val beers: List<Beer>
)

interface UntappdApi {
    @GET("search/beer")
    suspend fun searchBeer(
        @Query("q") query: String,
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String
    ): SearchResponse
    
    @GET("beer/info")
    suspend fun getBeerDetails(
        @Query("bid") beerId: String,
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String
    ): Beer
}

object UntappdApiService {
    private const val BASE_URL = "https://api.untappd.com/v4/"
    
    // These would be securely stored in a real app
    private const val CLIENT_ID = "YOUR_CLIENT_ID"
    private const val CLIENT_SECRET = "YOUR_CLIENT_SECRET"
    
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
    
    val api: UntappdApi = retrofit.create(UntappdApi::class.java)
    
    suspend fun searchBeer(query: String): Result<List<Beer>> {
        return try {
            val response = api.searchBeer(query, CLIENT_ID, CLIENT_SECRET)
            Result.success(response.beers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBeerDetails(beerId: String): Result<Beer> {
        return try {
            val beer = api.getBeerDetails(beerId, CLIENT_ID, CLIENT_SECRET)
            Result.success(beer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 