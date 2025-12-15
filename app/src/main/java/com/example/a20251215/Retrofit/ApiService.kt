package com.example.a20251215.Retrofit

import com.example.a20251215.LoginResponse
import com.example.a20251215.Post.PostListResponse
import com.example.a20251215.Retrofit.ApiResponse
import com.example.a20251215.Sign.SignupResponse

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    @FormUrlEncoded
    @POST("signup.php")
    fun signup(
        @Field("username") username: String,
        @Field("nickname") nickname: String,
        @Field("loginid") loginid: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<SignupResponse>

    @FormUrlEncoded
    @POST("send_code.php")
    fun sendEmailCode(
        @Field("email") email: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("check_code.php")
    fun checkEmailCode(
        @Field("email") email: String,
        @Field("code") code: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("loginid") loginid: String,
        @Field("password") password: String
    ): Call<LoginResponse>


    @FormUrlEncoded
    @POST("upload_post.php")
    fun uploadPost(
        @Field("member_id") memberId: Int,
        @Field("title") title: String,
        @Field("content") content: String,
        @Field("image_url") imageUrl: String
    ): Call<ApiResponse>


    @FormUrlEncoded
    @POST("update_post.php")
    fun updatePost(
        @Field("post_id") postId: Int,
        @Field("member_id") memberId: Int,
        @Field("title") title: String,
        @Field("content") content: String,
        @Field("image_url") imageUrl: String
    ): Call<ApiResponse>


    @FormUrlEncoded
    @POST("delete_post.php")
    fun deletePost(
        @Field("post_id") postId: Int,
        @Field("member_id") memberId: Int
    ): Call<ApiResponse>


    @FormUrlEncoded
    @POST("get_my_posts.php")
    fun getMyPosts(
        @Field("member_id") memberId: Int
    ): Call<PostListResponse>


    @FormUrlEncoded
    @POST("get_user_posts.php")
    fun getUserPosts(
        @Field("member_id") memberId: Int
    ): Call<PostListResponse>
}
