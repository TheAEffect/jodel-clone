<script>
    import { Router, Route } from "svelte-routing";
    import Header from "components/Header.svelte";
    import {useAuth} from "./contexts/AuthContext";
    import {useGeolocation} from "./hooks/UseGeolocation";
    import "smelte/src/tailwind.css" ;
    import Redirect from "components/Redirect.svelte";
    import Login from "./pages/Login.svelte";
    import Posts from "./pages/Posts.svelte";
    import {getRandomColor} from "./utils/Color";
    import {onMount} from "svelte";
    useGeolocation();
    useAuth().whoAmI();

    let rndColor;

    /**
     * Select one of the colors randomly
     * @returns {string}
     */
    const getColor = function() {
        rndColor = getRandomColor();
    }

    /**
     * Runs when componenent loaded
     */
    onMount(() => {
        getColor();
    });
</script>

<Router>
    <Header bind:rndColor={rndColor}/>
    <Route path="/">
        <Redirect to="/posts"
                  authRequired={true}/>
    </Route>
    <Route path="/login">
        <Login rndColor={rndColor}
               --mdc-theme-primary={rndColor}/>
    </Route>
    <Route path="/register">
        <Login rndColor={rndColor}
               --mdc-theme-primary={rndColor}
               register/>
    </Route>
    <Route path="/post/:id" let:params>
        <Posts singlePost={true}
               id={params.id}/>
    </Route>
    <Route path="/posts">
        <Posts rndColor={rndColor}
               --mdc-theme-primary={rndColor}/>
    </Route>
    <Route>
        <Redirect to="/404" />
    </Route>
</Router>
