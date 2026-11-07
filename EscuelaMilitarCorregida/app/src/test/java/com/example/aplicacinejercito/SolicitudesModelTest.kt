package com.example.aplicacinejercito

import com.example.aplicacinejercito.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SolicitudesModelTest {
    private val usuarios = listOf(
        UsuarioModel("u1", "Teniente Soto", "v.soto@escuelamilitar.cl", Grado.TENIENTE),
        UsuarioModel("u2", "Cadete Leiva", "m.leiva@escuelamilitar.cl", Grado.CADETE)
    )
    private val datos = DatosApp(
        usuarios = usuarios,
        reservas = listOf(
            ReservaModel("r1", "u1", "c1", "2026-12-01", 10, EstadoReserva.PENDIENTE, "2026-11-30"),
            ReservaModel("r2", "u2", "c1", "2026-12-01", 9, EstadoReserva.PENDIENTE, "2026-11-30"),
            ReservaModel("r3", "u2", "c2", "2026-11-20", 15, EstadoReserva.CONFIRMADA, "2026-11-16"),
            ReservaModel("r4", "u1", "c2", "2026-11-21", 11, EstadoReserva.RECHAZADA, "2026-11-16")
        )
    )

    @Test fun filtraPendientesPorDefecto() {
        val ids = SolicitudesModel.filtrar(datos, SolicitudesUiState()).map { it.id }
        assertEquals(listOf("r2", "r1"), ids)
    }

    @Test fun filtraProcesadasSinPendientes() {
        val ids = SolicitudesModel.filtrar(datos, SolicitudesUiState(procesadas = true)).map { it.id }
        assertEquals(listOf("r3", "r4"), ids)
    }

    @Test fun filtraPorGrado() {
        val ids = SolicitudesModel.filtrar(datos, SolicitudesUiState(grado = "Teniente")).map { it.id }
        assertEquals(listOf("r1"), ids)
    }

    @Test fun contaPorGradoSobrePestanaActiva() {
        val conteo = SolicitudesModel.contarPorGrado(datos, procesadas = false)
        assertEquals(listOf("Todos", "Teniente", "Cabo", "Cadete"), conteo.map { it.clave })
        assertEquals(listOf(2, 1, 0, 1), conteo.map { it.cantidad })
        assertEquals(listOf("Todos", "Tenientes", "Cabos", "Cadetes"), conteo.map { it.etiqueta })
    }

    @Test fun soloPendientesSonSeleccionables() {
        val reservas = datos.reservas
        assertEquals(setOf("r1", "r2"), SolicitudesModel.seleccionables(reservas))
    }

    @Test fun otroExigeTextoYLimite() {
        assertEquals("Escribe el motivo del rechazo.", SolicitudesModel.errorRechazo(RechazoFormulario(MotivoRechazo.OTRO, "   ")))
        assertEquals("El motivo no puede superar los 200 caracteres.", SolicitudesModel.errorRechazo(RechazoFormulario(MotivoRechazo.OTRO, "x".repeat(201))))
        assertNull(SolicitudesModel.errorRechazo(RechazoFormulario(MotivoRechazo.OTRO, "Revista de tropas")))
        assertNull(SolicitudesModel.errorRechazo(RechazoFormulario(MotivoRechazo.CUPO)))
    }

    @Test fun textoMotivoUsaEtiquetaOTextoLibre() {
        assertEquals("Cupo de la instalación completo", SolicitudesModel.textoMotivo(RechazoFormulario(MotivoRechazo.CUPO)))
        assertEquals("Revista", SolicitudesModel.textoMotivo(RechazoFormulario(MotivoRechazo.OTRO, "  Revista  ")))
    }

    @Test fun contadoresDePestanas() {
        assertEquals(2, SolicitudesModel.contarPendientes(datos))
        assertEquals(2, SolicitudesModel.contarProcesadas(datos))
    }
}
