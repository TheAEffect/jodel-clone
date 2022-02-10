<script>
    export let top = false;
    export let bottom = false;
    export let color = undefined;
    export let overflow = "visible";
    export let scale = false;
</script>
<div class:bottom
     class:top
     class:scale
     class="container-fluid"
     style="overflow: {overflow}">
    <div class="container"
         style="background-color:{color}">
        <slot></slot>
    </div>
</div>
