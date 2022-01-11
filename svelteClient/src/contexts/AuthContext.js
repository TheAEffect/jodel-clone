import {axiosAPI} from "../apis/AxiosQuarkus.js";
import {storeChannel, storeGeo, storeOrder, storeSection, storeUser} from '../utils/store.js';
import { navigate } from "svelte-routing";

export const useAuth = function() {
    /**
     * Subscribe to user on mount
     * Because this sets state in the callback it will cause any
     * component that utilizes this hook to re-render with the
     * latest auth object.
     */
    const whoAmI = async () => {
        axiosAPI().get("/user/whoami")
            .then((res) => {
                if (res.status === 200) {
                    storeUser.set(res.data);
                    if (location.pathname === "/login" || location.pathname === "/register" || location.pathname === "/") {
                        navigate("/posts", { replace: true });
                    }
                } else {
                    reset();
                    navigate("/login", { replace: true });
                }
            })
            .catch(() => {
                reset();
                navigate("/login", { replace: true });
            });
    };


    /**
     * Resets the store
     */
    const reset = () => {
        storeUser.set(false);
        storeOrder.set(undefined);
        storeGeo.set(undefined);
        storeSection.set(undefined);
        storeChannel.set(undefined);
    }
    /**
     * login for the website
     * @param username
     * @param password
     * @returns {Promise<boolean>}
     */
    const login = async (username, password) => {
        try {
            const res = await axiosAPI().post("/login", {username, password});

            if (res.status === 200) {
                storeUser.set(res.data);
                return true;
            }

        } catch (error) {
            throw error;
        }
    };

    /**
     * register for the website
     * @param email
     * @param password
     * @param password_confirm
     * @returns {Promise<boolean>}
     */
    const register = async (email, password, password_confirm) => {
        try {
            const res = await axiosAPI().post('/register', {email, password, password_confirm});

            if (res.status === 201) {
                return res.data;
            }

        } catch (error) {
            throw error;
        }
    }

    /**
     * logout from website
     */
    const logout = () => {
        axiosAPI().post("/logout").then(res => {
            if (res.status === 200) {
                reset();
                location.reload();
            }
        }).catch(error => {
            reset();
            throw error;
        });
    };

    /**
     * edit the profile
     * @param data
     */
    const editUser = async (data) => {
        try {
            const res = await axiosAPI().patch('/user', data);

            if (res.status === 200) {
                storeUser.set({...storeUser, ...res.data});
                return res.data;
            }

        } catch (error) {
            throw error;
        }
    }


    /**
     * delete the profile
     */
    const deleteUser = () => axiosAPI().delete(`/user`)
        .then(() => {
            location.href = '/login';
        })
        .catch(error => {throw error});

    // Return the user object and auth methods
    return {
        whoAmI,
        login,
        register,
        logout,
        editUser,
        deleteUser
    };
};