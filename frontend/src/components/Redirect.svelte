<script>
    import { onMount } from 'svelte';
    import { navigate } from 'svelte-routing';
    import {storeUser} from "../utils/store";

    export let to;
    export let authRequired;

    /**
     * Runs when componenent loaded
     */
    onMount(() => {
        if(authRequired && !$storeUser) {
            navigate('/login')
        } else {
            navigate(to);
        }
    });
</script>