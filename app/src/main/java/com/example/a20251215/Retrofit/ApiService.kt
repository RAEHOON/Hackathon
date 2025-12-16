package com.example.a20251215.Retrofit

import com.example.a20251215.Find.FindIdResponse
import com.example.a20251215.LoginResponse
import com.example.a20251215.Post.PostDetailResponse
import com.example.a20251215.Post.PostListResponse
import com.example.a20251215.Ranking.RankingResponse
import com.example.a20251215.Retrofit.ApiResponse
import com.example.a20251215.Sign.SignupResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

import retrofit2.Call

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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


    @Multipart
    @POST("upload_post.php")
    fun uploadPost(
        @Part("member_id") memberId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<ApiResponse>


    @Multipart
    @POST("update_post.php")
    fun updatePost(
        @Part("post_id") postId: RequestBody,
        @Part("member_id") memberId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part image: MultipartBody.Part? = null
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

    @FormUrlEncoded
    @POST("get_post_detail.php")
    fun getPostDetail(
        @Field("post_id") postId: Int
    ): Call<PostDetailResponse>




    @FormUrlEncoded
    @POST("check_id.php")
    fun checkIdDuplicate(
        @Field("loginid") loginId: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("check_nickname.php")
    fun checkNicknameDuplicate(
        @Field("nickname") nickname: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("check_email.php")
    fun checkEmailDuplicate(
        @Field("email") email: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("update_nickname.php")
    fun updateNickname(
        @Field("member_id") memberId: Int,
        @Field("nickname") nickname: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("reset_password.php")
    fun resetPassword(
        @Field("member_id") memberId: Int,
        @Field("current_password") currentPassword: String,
        @Field("new_password") newPassword: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("delete_account.php")
    fun deleteAccount(
        @Field("member_id") memberId: Int
    ): Call<ApiResponse>


    @FormUrlEncoded
    @POST("find_id.php")
    fun findId(
        @Field("username") username: String,
        @Field("email") email: String
    ): Call<FindIdResponse>

    @FormUrlEncoded
    @POST("find_password.php")
    fun findPassword(
        @Field("loginid") loginid: String,
        @Field("email") email: String,
        @Field("code") code: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("get_best_ranking.php")
    fun getBestRanking(
        @Field("month") month: String
    ): Call<RankingResponse>

    @FormUrlEncoded
    @POST("get_worst_ranking.php")
    fun getWorstRanking(
        @Field("month") month: String
    ): Call<RankingResponse>

    @FormUrlEncoded
    @POST("get_user_by_date.php")
    fun getUsersByDate(
        @Field("date") date: String
    ): Call<RankingResponse>

    @FormUrlEncoded
    @POST("reset_password_by_email.php")
    fun resetPasswordByEmail(
        @Field("loginid") loginid: String,
        @Field("email") email: String,
        @Field("new_password") newPassword: String
    ): Call<ApiResponse>





}
