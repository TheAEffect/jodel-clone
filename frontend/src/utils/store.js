import { writable } from 'svelte/store';

export const storeUser = writable(false);
export const storeSection = writable(null);
export const storeChannel = writable(null);
export const storeGeo = writable(null);
export const storeOrder = writable(undefined);