package com.example.miprimeraplicacion;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HouseUtils {

    /**
     * Filtra las casas para incluir solo aquellas que pertenecen al usuario actual.
     *
     * @param housesData JSON con todas las casas disponibles
     * @param username   Usuario propietario de las casas
     * @return JSON en formato String con solo las casas del usuario
     */
    public static String filterUserHouses(String housesData, String username) {
        try {
            JSONArray allHouses = new JSONArray(housesData);
            JSONArray userHouses = new JSONArray();

            for (int i = 0; i < allHouses.length(); i++) {
                JSONObject house = allHouses.getJSONObject(i);
                if (house.getString("username").equals(username)) {
                    userHouses.put(house);
                }
            }

            return userHouses.toString();  // Devuelve JSON solo con las casas del usuario
        } catch (JSONException e) {
            e.printStackTrace();
            return "[]"; // Devuelve un JSON vacío en caso de error
        }
    }
}
