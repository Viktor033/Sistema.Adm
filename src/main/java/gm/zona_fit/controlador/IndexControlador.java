package gm.zona_fit.controlador;

import gm.zona_fit.modelo.Cliente;
import gm.zona_fit.servicio.IClienteServicio;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Data;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
@Data
public class IndexControlador implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(IndexControlador.class);

    private List<Cliente> clientes;
    private Cliente clienteSeleccionado;
    private String criterioBusqueda;

    private final IClienteServicio clienteServicio;

    public IndexControlador(IClienteServicio clienteServicio) {
        this.clienteServicio = clienteServicio;
    }

    @PostConstruct
    public void init() {
        cargarDatos();
    }

    public void cargarDatos() {
        this.clientes = clienteServicio.listarClientes();
        clientes.forEach(cliente -> logger.info(cliente.toString()));
    }

    public void agregarCliente() {
        clienteSeleccionado = new Cliente();
    }

    public void guardarCliente() {
        logger.info("Cliente a guardar: " + clienteSeleccionado);

        clienteServicio.guardarCliente(clienteSeleccionado);

        if (clienteSeleccionado.getDni() == null) {
            mostrarMensaje("Cliente agregado correctamente");
        } else {
            mostrarMensaje("Cliente actualizado correctamente");
        }

        cargarDatos(); // ✅ Recarga la lista desde la base
        clienteSeleccionado = null;

        PrimeFaces.current().executeScript("PF('ventanaModalCliente').hide()");
        PrimeFaces.current().ajax().update("forma-clientes:mensajes", "forma-clientes:clientes-tabla");
    }


    public void eliminarCliente() {
        logger.info("Cliente a eliminar: " + clienteSeleccionado);
        clienteServicio.eliminarCliente(clienteSeleccionado);
        clientes.remove(clienteSeleccionado);
        clienteSeleccionado = null;
        mostrarMensaje("Cliente eliminado correctamente");
        PrimeFaces.current().ajax().update("forma-clientes:mensajes", "forma-clientes:clientes-tabla");
    }

    public void buscarClientes() {
        if (criterioBusqueda == null || criterioBusqueda.trim().isEmpty()) {
            cargarDatos();
            return;
        }

        String criterio = criterioBusqueda.trim().toLowerCase();
        clientes = clienteServicio.listarClientes().stream()
                .filter(c -> c.getNombre().toLowerCase().contains(criterio)
                        || String.valueOf(c.getDni()).contains(criterio))
                .toList();

        mostrarMensaje("Se encontraron " + clientes.size() + " coincidencias");
        PrimeFaces.current().ajax().update("forma-clientes:mensajes", "forma-clientes:clientes-tabla");
    }

    private void mostrarMensaje(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, mensaje, null));
    }
}
