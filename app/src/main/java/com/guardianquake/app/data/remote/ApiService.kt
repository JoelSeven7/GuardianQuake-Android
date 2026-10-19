package com.guardianquake.app.data.remote

import com.guardianquake.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ---------- Usuarios / autenticacion ----------
    @POST("api/usuarios/registro")
    suspend fun registro(@Body body: UsuarioRegistroRequest): Response<UsuarioOut>

    @FormUrlEncoded
    @POST("api/usuarios/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<TokenResponse>

    @GET("api/usuarios/me")
    suspend fun perfil(): Response<UsuarioOut>

    // ---------- Dispositivos ----------
    @POST("api/dispositivos")
    suspend fun registrarDispositivo(@Body body: DispositivoCrearRequest): Response<DispositivoOut>

    @GET("api/dispositivos/{id}/calibracion")
    suspend fun obtenerCalibracion(@Path("id") idDispositivo: Int): Response<CalibracionOut>

    // ---------- Eventos sismicos ----------
    @POST("api/eventos")
    suspend fun registrarEvento(@Body body: EventoSismicoRequest): Response<EventoSismicoOut>

    @GET("api/eventos")
    suspend fun listarEventos(
        @Query("desde") desde: String? = null,
        @Query("hasta") hasta: String? = null
    ): Response<List<EventoSismicoOut>>

    @GET("api/eventos/{id}")
    suspend fun obtenerEvento(@Path("id") idEvento: Int): Response<EventoSismicoOut>

    // ---------- Contenido educativo ----------
    @GET("api/contenido-educativo")
    suspend fun listarContenido(): Response<List<ContenidoEducativoOut>>

    @GET("api/contenido-educativo/{id}")
    suspend fun obtenerContenido(@Path("id") idContenido: Int): Response<ContenidoEducativoOut>

    // ---------- Suscripcion Pro ----------
    @GET("api/suscripcion/estado")
    suspend fun estadoSuscripcion(): Response<EstadoSuscripcionOut>

    @POST("api/suscripcion/verificar-compra")
    suspend fun verificarCompra(@Body body: VerificarCompraRequest): Response<SuscripcionOut>

    @POST("api/suscripcion/cancelar")
    suspend fun cancelarSuscripcion(): Response<SuscripcionOut>

    // ---------- Contactos de emergencia ----------
    @GET("api/contactos-emergencia")
    suspend fun listarContactos(): Response<List<ContactoEmergenciaOut>>

    @POST("api/contactos-emergencia")
    suspend fun crearContacto(@Body body: ContactoEmergenciaRequest): Response<ContactoEmergenciaOut>

    @DELETE("api/contactos-emergencia/{id}")
    suspend fun eliminarContacto(@Path("id") idContacto: Int): Response<Unit>
}
