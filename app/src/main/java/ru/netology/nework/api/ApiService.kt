package ru.netology.nework.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.dto.CreatePostRequest
import ru.netology.nework.dto.EventDto
import ru.netology.nework.dto.MediaDto
import ru.netology.nework.dto.PostDto
import ru.netology.nework.dto.TokenDto
import ru.netology.nework.dto.UserDto


interface ApiService {

    @POST("api/users/authentication")
    suspend fun login(
        @Query("login") login: String,
        @Query("pass") pass: String
    ): Response<TokenDto>

    @Multipart
    @POST("api/users/registration")
    suspend fun register(
        @Part("login") login: RequestBody,
        @Part("pass") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part avatar: MultipartBody.Part? = null
    ): Response<TokenDto>

    @Multipart
    @POST("api/media")
    suspend fun uploadMedia(
        @Part mediaFile: MultipartBody.Part
    ): Response<MediaDto>

    @GET("api/posts")
    suspend fun getPosts(): Response<List<PostDto>>

    @GET("api/posts/latest")
    suspend fun getLatestPosts(@Query("count") count: Int): Response<List<PostDto>>

    @GET("api/posts/{id}/newer")
    suspend fun getNewerPosts(@Path("id") id: Long): Response<List<PostDto>>

    @GET("api/posts/{id}")
    suspend fun getPostById(@Path("id") id: Long): Response<PostDto>

    @POST("api/posts")
    suspend fun savePost(@Body post: CreatePostRequest): Response<PostDto>

    @DELETE("api/posts/{id}")
    suspend fun deletePostById(@Path("id") id: Long): Response<Unit>

    @POST("api/posts/{id}/likes")
    suspend fun likePostById(@Path("id") id: Long): Response<PostDto>

    @DELETE("api/posts/{id}/likes")
    suspend fun unlikePostById(@Path("id") id: Long): Response<PostDto>

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<UserDto>


    //    API EVENTS:
    @GET("api/events")
    suspend fun getEvents(): Response<List<EventDto>>

    //    API USERS:
    @GET("api/users")
    suspend fun getUsers(): Response<List<UserDto>>


}