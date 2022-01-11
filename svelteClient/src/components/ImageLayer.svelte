<script>
    import {onDestroy, onMount} from "svelte";

    export let post;
    export let bgImage;
    export let released;
    let timeout;
    let img = undefined;
    let container;
    let display;

    /**
     * Runs when componenent loaded
     */
    onMount(()=>{
        if (post.type === "IMAGE") {
            timeout = setTimeout(() => {
                img = document.createElement("IMG");
                img.setAttribute("src", bgImage);
                img.setAttribute("class", "overlayImg");
                img.setAttribute("draggable","false");
                container.appendChild(img);
                display = true;
            }, 1000);
        }
    })

    /**
     * Runs when componenent destroys
     */
    onDestroy(() => {
        window.clearTimeout(timeout);
    });
</script>
<svelte:window on:mouseup={released} on:touchend={released} />
<svelte:head>
    <style>
        .jodel-card-style.img:hover {
            cursor: grabbing;
        }
    </style>
    {#if display}
        <style>
            body {
                overflow: hidden;
            }
        </style>
    {/if}
</svelte:head>
<div class="overlayDiv"
     class:display={display}>
    <div bind:this={container}
         class="container"></div>
</div>