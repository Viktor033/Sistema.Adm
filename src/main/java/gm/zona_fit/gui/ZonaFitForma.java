package gm.zona_fit.gui;

import gm.zona_fit.modelo.Cliente;
import gm.zona_fit.servicio.ClienteServicio;
import gm.zona_fit.servicio.IClienteServicio;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ZonaFitForma extends JFrame {
    private JPanel panelPrincipal;
    private JTable clientesTabla;
    private JTextField nombreTexto;
    private JTextField apellidoTexto;
    private JTextField telefonoTexto;
    private JTextField membresiaTexto;
    private JTextField dniTexto;
    private JButton guardarButton;
    private JButton eliminarButton;
    private JButton limpiarButton;
    private JTextField buscarTexto;
    private JButton buscarButton;

    private DefaultTableModel tablaModeloClientes;
    private Integer idCliente;

    @Autowired
    IClienteServicio clienteServicio;

    @Autowired
    public ZonaFitForma(ClienteServicio clienteServicio) {
        this.clienteServicio = clienteServicio;
        iniciarForma();

        guardarButton.addActionListener(e -> guardarCliente());
        eliminarButton.addActionListener(e -> eliminarCliente());
        limpiarButton.addActionListener(e -> limpiarFormulario());
        buscarButton.addActionListener(e -> buscarClientes());

        clientesTabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                cargarClienteSeleccionado();
            }
        });
    }

    private void iniciarForma() {
        setContentPane(panelPrincipal);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        createUIComponents();
    }

    private void createUIComponents() {
        this.tablaModeloClientes = new DefaultTableModel(0, 5) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        String[] cabeceros = {"DNI", "Nombre", "Apellido", "Teléfono", "Membresía"};
        this.tablaModeloClientes.setColumnIdentifiers(cabeceros);
        this.clientesTabla = new JTable(tablaModeloClientes);
        this.clientesTabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listarClientes();
    }

    private void listarClientes() {
        tablaModeloClientes.setRowCount(0); // Limpia la tabla
        List<Cliente> clientes = clienteServicio.listarClientes();
        for (Cliente cliente : clientes) {
            Object[] fila = {
                    cliente.getDni(),
                    cliente.getNombre(),
                    cliente.getApellido(),
                    cliente.getTelefono(),
                    cliente.getMembresia()
            };
            tablaModeloClientes.addRow(fila);
        }
        // 🔹 Fuerza a la tabla a refrescarse inmediatamente
        tablaModeloClientes.fireTableDataChanged();
    }


    private void buscarClientes() {
        String textoBusqueda = buscarTexto.getText().trim().toLowerCase();
        tablaModeloClientes.setRowCount(0);

        List<Cliente> clientes = clienteServicio.listarClientes();
        clientes.stream()
                .filter(c -> c.getNombre().toLowerCase().contains(textoBusqueda)
                        || c.getApellido().toLowerCase().contains(textoBusqueda))
                .forEach(c -> {
                    Object[] fila = {
                            c.getDni(),
                            c.getNombre(),
                            c.getApellido(),
                            c.getTelefono(),
                            c.getMembresia()
                    };
                    tablaModeloClientes.addRow(fila);
                });

        if (tablaModeloClientes.getRowCount() == 0) {
            mostrarMensaje("No se encontraron clientes con ese nombre o apellido");
        }
    }

    private void guardarCliente() {
        // Validaciones básicas
        if (dniTexto.getText().trim().isEmpty()) {
            mostrarMensaje("Proporciona un DNI");
            dniTexto.requestFocusInWindow();
            return;
        }

        if (nombreTexto.getText().trim().isEmpty()) {
            mostrarMensaje("Proporciona un nombre");
            nombreTexto.requestFocusInWindow();
            return;
        }

        if (membresiaTexto.getText().trim().isEmpty()) {
            mostrarMensaje("Proporciona una membresía");
            membresiaTexto.requestFocusInWindow();
            return;
        }

        try {
            // Parseo de campos
            int dni = Integer.parseInt(dniTexto.getText().trim());
            String nombre = nombreTexto.getText().trim();
            String apellido = apellidoTexto.getText().trim();

            Integer telefono = null;
            if (!telefonoTexto.getText().trim().isEmpty()) {
                telefono = Integer.valueOf(telefonoTexto.getText().trim());
            }

            Integer membresia = Integer.valueOf(membresiaTexto.getText().trim());

            // Verificación de cliente existente
            Cliente clienteExistente = clienteServicio.buscarClientePorId(dni);
            if (clienteExistente != null) {
                mostrarMensaje("Ya existe un cliente con ese DNI");
                return;
            }

            // Crear y guardar cliente
            Cliente nuevoCliente = new Cliente(dni, nombre, apellido, telefono, membresia);
            clienteServicio.guardarCliente(nuevoCliente);

            // 🔹 Actualizar tabla automáticamente
            listarClientes();

            // 🔹 Limpiar formulario al final
            limpiarFormulario();

            mostrarMensaje("Cliente guardado correctamente");

        } catch (NumberFormatException e) {
            mostrarMensaje("Verifica que DNI, teléfono y membresía sean números válidos");
        } catch (Exception e) {
            mostrarMensaje("Ocurrió un error al guardar el cliente: " + e.getMessage());
        }
    }


    private void cargarClienteSeleccionado() {
        int renglon = clientesTabla.getSelectedRow();
        if (renglon != -1) {
            idCliente = Integer.parseInt(clientesTabla.getModel().getValueAt(renglon, 0).toString());
            nombreTexto.setText(clientesTabla.getModel().getValueAt(renglon, 1).toString());
            apellidoTexto.setText(clientesTabla.getModel().getValueAt(renglon, 2).toString());
            telefonoTexto.setText(clientesTabla.getModel().getValueAt(renglon, 3).toString());
            membresiaTexto.setText(clientesTabla.getModel().getValueAt(renglon, 4).toString());
        }
    }

    private void eliminarCliente() {
        int renglon = clientesTabla.getSelectedRow();
        if (renglon != -1) {
            int dni = Integer.parseInt(clientesTabla.getModel().getValueAt(renglon, 0).toString());
            Cliente cliente = clienteServicio.buscarClientePorId(dni);
            if (cliente != null) {
                clienteServicio.eliminarCliente(cliente);
                mostrarMensaje("Cliente con DNI " + dni + " eliminado");
                limpiarFormulario();
                listarClientes();
            } else {
                mostrarMensaje("No se encontró el cliente para eliminar");
            }
        } else {
            mostrarMensaje("Debe seleccionar un Cliente a eliminar");
        }
    }

    private void limpiarFormulario() {
        dniTexto.setText("");
        nombreTexto.setText("");
        apellidoTexto.setText("");
        telefonoTexto.setText("");
        membresiaTexto.setText("");
        idCliente = null;
        clientesTabla.getSelectionModel().clearSelection();
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje);
    }
}
