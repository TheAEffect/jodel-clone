import axios from "axios";
import {onMount} from "svelte";
import {storeGeo} from "../utils/store";

/**
 * Hook for Geolocation
 * @returns {{getLocation: (function(): {city: null, latitude: null, longitude: null})}}
 * @constructor
 */
export const useGeolocation = () => {

    /**
     * Gets the city to given longitude and latitude and sets the state
     * @param longitude
     * @param latitude
     */
    const callPhoton = (longitude, latitude) => {
        axios.get(`https://photon.komoot.io/reverse?lon=${longitude}&lat=${latitude}`)
            .then((res) => {
                const city = res.data?.features?.[0]?.properties?.city || "Stuttgart";
                storeGeo.set({longitude, latitude, city});
            })
            .catch(() => {
                storeGeo.set({longitude, latitude, city: "Stuttgart"});
            });
    };

    /**
     * Fallback method if navigator geolocation fails or is disabled
     * Provides geodata via IP lookup
     */
    const fallback = () => {
        axios.get(`https://ipwho.is/`)
            .then((res) => {
                if (res.data && res.data.success !== false) {
                    storeGeo.set({
                        longitude: res.data.longitude || 9.1829,
                        latitude: res.data.latitude || 48.7758,
                        city: res.data.city || "Stuttgart"
                    });
                } else {
                    storeGeo.set({longitude: 9.1829, latitude: 48.7758, city: "Stuttgart"});
                }
            })
            .catch(() => {
                storeGeo.set({longitude: 9.1829, latitude: 48.7758, city: "Stuttgart"});
            });
    };

    /**
     * Will be called only once
     * Gets longitude and latitude via 2 different ways
     * 1. If navigator.geolocation enabled: get coords
     * 2. If navigator.geolocation disabled: get ip address & afterwards get coords
     * Finally calls the function callPhoton
     */
    onMount(() => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(position => {
                callPhoton(position.coords.longitude, position.coords.latitude);
            }, error => {
                fallback();
            }, {
                timeout: 5000,
                maximumAge: 60000
            });
        } else {
            fallback();
        }
    });
};