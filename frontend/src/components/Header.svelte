<script>
    import { Navbar, NavbarBrand } from 'sveltestrap';
    import {getRandomColor} from "../utils/Color";
    import { navigate } from 'svelte-routing';

    export let rndColor = null;
</script>

<Navbar id="jodel-header"
        db="dark"
        expand="md"
        variant="dark">
    <div class="container">
        <NavbarBrand id="jodel-home"
                     on:click={(e) => {e.preventDefault(); rndColor = getRandomColor(); navigate("/")}}>
            <img alt="fox logo"
                 src='/images/fox.png'
                 class="logo" />
            <span style="color:{rndColor}">Jodel</span>
        </NavbarBrand>
    </div>
</Navbar>
<slot></slot>