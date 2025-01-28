package com.example.miprimeraplicacion;
import android.util.Log;
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
    private List<Long> disabledDays = new ArrayList<>();

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
        Log.d("Reserva", "📌 Cargando fechas bloqueadas para casa ID: " + houseId);
        MainActivity.getBlockedDates(houseId, new MainActivity.LoginResponseCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Log.d("Reserva", "📥 Respuesta del servidor: " + response);
                    JSONObject jsonResponse = new JSONObject(response);

                    if (jsonResponse.getString("status").equals("success")) {
                        JSONArray blockedDates = jsonResponse.getJSONArray("blocked_dates");

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        disabledDays.clear(); // Limpiar fechas previas

                        for (int i = 0; i < blockedDates.length(); i++) {
                            JSONObject dateRange = blockedDates.getJSONObject(i);
                            try {
                                long start = sdf.parse(dateRange.getString("check_in")).getTime();
                                long end = sdf.parse(dateRange.getString("check_out")).getTime();

                                Log.d("Reserva", "🔎 Bloqueado: " + dateRange.getString("check_in") + " - " + dateRange.getString("check_out"));

                                for (long date = start; date <= end; date += 86400000) {
                                    disabledDays.add(date);
                                }
                            } catch (java.text.ParseException e) {
                                Log.e("Reserva", "❌ Error al parsear fechas bloqueadas", e);
                            }
                        }

                        requireActivity().runOnUiThread(() -> showDateRangePicker(disabledDays));
                    } else {
                        Log.e("Reserva", "⚠️ Error en la respuesta del servidor: " + jsonResponse.optString("message"));
                    }
                } catch (JSONException e) {
                    Log.e("Reserva", "❌ Error al procesar la respuesta JSON", e);
                }
            }

            @Override
            public void onError(String error) {
                Log.e("Reserva", "🚨 Error al cargar fechas bloqueadas: " + error);
            }
        });
    }

    private void showConfirmationDialog() {
        if (houseId == null || checkInDate == null || checkOutDate == null) {
            textViewSelectedDates.setText("Error: Falta información para la reserva.");
            return;
        }

        if (isDateBlocked(checkInDate, checkOutDate)) {
            Log.e("Reserva", "🚨 Error: Intento de reserva en fechas bloqueadas.");
            new AlertDialog.Builder(requireContext())
                    .setTitle("Reserva no permitida")
                    .setMessage("Las fechas seleccionadas ya están ocupadas. Elija otras fechas.")
                    .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Reserva")
                .setMessage("¿Confirmar reserva del " + checkInDate + " al " + checkOutDate + "?")
                .setPositiveButton("Sí", (dialog, which) -> sendReservationRequest())
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private boolean isDateBlocked(String checkIn, String checkOut) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            long start = sdf.parse(checkIn).getTime();
            long end = sdf.parse(checkOut).getTime();

            for (long date = start; date <= end; date += 86400000) {
                if (disabledDays.contains(date)) {
                    Log.d("Reserva", "⛔ Fecha bloqueada detectada: " + sdf.format(new Date(date)));
                    return true; // Encontró una fecha bloqueada
                }
            }
        } catch (java.text.ParseException e) {
            Log.e("Reserva", "❌ Error al verificar fechas bloqueadas", e);
        }
        return false;
    }

    private void sendReservationRequest() {
        if (houseId == null || checkInDate == null || checkOutDate == null) {
            Log.e("Reserva", "❌ Faltan datos para la reserva.");
            textViewSelectedDates.setText("Error: Falta información para la reserva.");
            return;
        }

        Log.d("Reserva", "📤 Enviando solicitud de reserva para casa " + houseId + " (" + checkInDate + " - " + checkOutDate + ")");

        MainActivity.reserveHouse(userLogged, houseId, checkInDate, checkOutDate, new MainActivity.LoginResponseCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    if (response == null || response.isEmpty()) {
                        Log.e("Reserva", "❌ Respuesta vacía del servidor.");
                        getActivity().runOnUiThread(() ->
                                textViewSelectedDates.setText("Error: Respuesta vacía del servidor")
                        );
                        return;
                    }

                    JSONObject jsonResponse = new JSONObject(response);
                    Log.d("Reserva", "📥 Respuesta recibida: " + response);

                    if (jsonResponse.optString("status").equals("success")) {
                        if (jsonResponse.has("reservation_id")) {  // ✅ Verificar si la clave existe
                            String reservationId = jsonResponse.getString("reservation_id");  // ✅ Obtener el ID real
                            getActivity().runOnUiThread(() -> showConfirmationDialog(reservationId, userLogged, checkInDate, checkOutDate));
                        } else {
                            getActivity().runOnUiThread(() -> textViewSelectedDates.setText("Error: No se recibió el ID de la reserva"));
                        }
                    }

                } catch (JSONException e) {
                    Log.e("Reserva", "❌ Error al procesar la respuesta JSON", e);
                    e.printStackTrace();
                    getActivity().runOnUiThread(() ->
                            textViewSelectedDates.setText("Error al procesar la respuesta del servidor")
                    );
                }
            }


            @Override
            public void onError(String error) {
                Log.e("Reserva", "🚨 Error en la reserva: " + error);
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


