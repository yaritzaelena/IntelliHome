package com.example.miprimeraplicacion;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import androidx.appcompat.app.AlertDialog;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import android.app.Dialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;
import java.util.List;
import java.util.ArrayList;




public class ReserveHouseActivity extends DialogFragment {
    private String houseId, userLogged, checkInDate, checkOutDate;
    private TextView textViewSelectedDates;

    public static ReserveHouseActivity newInstance(String houseId, String userLogged) {
        ReserveHouseActivity fragment = new ReserveHouseActivity();
        Bundle args = new Bundle();
        args.putString("HOUSE_ID", houseId);
        args.putString("USER_LOGGED", userLogged);
        fragment.setArguments(args);
        return fragment;
    }
    private void showDateRangePicker(List<Long> disabledDays) {
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                .setValidator(new DateValidatorExcluding(disabledDays));

        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Selecciona tu fecha de reserva");
        builder.setCalendarConstraints(constraintsBuilder.build());

        MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();
        datePicker.show(getParentFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            checkInDate = sdf.format(new Date(selection.first));
            checkOutDate = sdf.format(new Date(selection.second));

            textViewSelectedDates.setText("Entrada: " + checkInDate + "  |  Salida: " + checkOutDate);
        });
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_reserve_house, null);

        houseId = getArguments().getString("HOUSE_ID");
        userLogged = getArguments().getString("USER_LOGGED");
        textViewSelectedDates = view.findViewById(R.id.textViewSelectedDates);
        Button buttonSelectDates = view.findViewById(R.id.buttonSelectDates);
        Button buttonConfirmReservation = view.findViewById(R.id.buttonConfirmReservation);
        Button buttonBack = view.findViewById(R.id.buttonBack);

        loadBlockedDates();
        buttonSelectDates.setOnClickListener(v -> showDateRangePicker(new ArrayList<>()));


        // Confirmar reserva (muestra el diálogo de confirmación)
        buttonConfirmReservation.setOnClickListener(v -> showConfirmationDialog());

        // Cerrar ventana emergente
        buttonBack.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }

    private void loadBlockedDates() {
        MainActivity.getBlockedDates(houseId, new MainActivity.LoginResponseCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getString("status").equals("success")) {
                        JSONArray blockedDates = jsonResponse.getJSONArray("blocked_dates");

                        List<Long> disabledDays = new ArrayList<>();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                        for (int i = 0; i < blockedDates.length(); i++) {
                            JSONObject dateRange = blockedDates.getJSONObject(i);
                            try {
                                long start = sdf.parse(dateRange.getString("check_in")).getTime();
                                long end = sdf.parse(dateRange.getString("check_out")).getTime();

                                // Agregar todos los días entre check-in y check-out a la lista de días bloqueados
                                for (long date = start; date <= end; date += 86400000) {  // 86400000 ms = 1 día
                                    disabledDays.add(date);
                                }
                            } catch (java.text.ParseException e) {
                                e.printStackTrace();  // Manejar error de conversión de fecha
                            }
                        }

                        requireActivity().runOnUiThread(() -> showDateRangePicker(disabledDays));
                    } else {
                        requireActivity().runOnUiThread(() -> textViewSelectedDates.setText("Error: " + jsonResponse.optString("message", "No se pudo obtener el mensaje de error")));

                    }
                } catch (org.json.JSONException e) {
                    e.printStackTrace();  // Manejar error de JSON
                }
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> textViewSelectedDates.setText("Error al cargar fechas bloqueadas: " + error));
            }
        });
    }

    private void showConfirmationDialog() {
        if (houseId == null || checkInDate == null || checkOutDate == null) {
            textViewSelectedDates.setText("Error: Falta información para la reserva.");
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Reserva")
                .setMessage("¿Confirmar reserva del " + checkInDate + " al " + checkOutDate + "?")
                .setPositiveButton("Sí", (dialog, which) -> sendReservationRequest())
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void sendReservationRequest() {
        if (houseId == null || checkInDate == null || checkOutDate == null) {
            textViewSelectedDates.setText("Error: Falta información para la reserva.");
            return;
        }

        MainActivity.reserveHouse(userLogged, houseId, checkInDate, checkOutDate, new MainActivity.LoginResponseCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (response == null || response.isEmpty()) {
                        getActivity().runOnUiThread(() ->
                                textViewSelectedDates.setText("Error: Respuesta vacía del servidor")
                        );
                        return;
                    }

                    JSONObject jsonResponse = new JSONObject(response);

                    if (jsonResponse.optString("status").equals("success")) {
                        if (jsonResponse.has("reservation_id")) {  // ✅ Verificar si la clave existe
                            String reservationId = jsonResponse.getString("reservation_id");  // ✅ Obtener el ID real
                            getActivity().runOnUiThread(() -> showConfirmationDialog(reservationId, userLogged, checkInDate, checkOutDate));
                        } else {
                            getActivity().runOnUiThread(() -> textViewSelectedDates.setText("Error: No se recibió el ID de la reserva"));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(() ->
                            textViewSelectedDates.setText("Error al procesar la respuesta del servidor")
                    );
                }
            }


            @Override
            public void onError(String error) {
                getActivity().runOnUiThread(() -> textViewSelectedDates.setText("Error en la reserva: " + error));
            }
        });
    }

    private void showConfirmationDialog(String reservationId, String user, String checkIn, String checkOut) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirmación de Reserva")
                .setMessage("¡Tu reserva ha sido confirmada!\n\n"
                        + "📌 Número de reserva: " + reservationId + "\n"
                        + "👤 Usuario: " + user + "\n"
                        + "📅 Check-in: " + checkIn + "\n"
                        + "📅 Check-out: " + checkOut)
                .setPositiveButton("Aceptar", (dialog, which) -> dismiss()) // Cierra la ventana emergente
                .show();
    }






}


