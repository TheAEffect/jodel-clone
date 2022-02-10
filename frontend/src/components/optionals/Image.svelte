<script>
    import {onMount} from "svelte";

    export let prop = null;
    export let color = undefined;
    export let text = false;
    let canvas;
    let imgCreate;
    let background = new Image();
    let loaded = false;
    let draggedNow = false;
    let ctx;
    let pos = { x: 0, y: 0 };
    let textfield = {
        value: undefined,
        bind: undefined
    }

    /**
     * Runs when componenent loaded
     */
    onMount(()=>{
        ctx = canvas.getContext("2d");
        canvas.width = imgCreate.clientWidth;
        canvas.height = imgCreate.clientHeight;
        ctx.fillStyle = color;
        ctx.fillRect(0, 0, canvas.width, canvas.height);
    })

    /**
     * If input is done
     */
    $:  {
            if(loaded === false) {
                 background.src = prop?.value;
                 background.onload = function () {
                     var scale = Math.max(ctx.canvas.width / background.width, ctx.canvas.height / background.height); // get the max scale to fit
                     var x = (ctx.canvas.width - (background.width * scale)) / 2;
                     var y = (ctx.canvas.height - (background.height * scale)) / 2;
                     ctx.drawImage(background, x, y, background.width * scale, background.height * scale);
                     loaded = true;
                 }
            }
        }

    /**
     * Sets the current position
     */
    function setPosition(e) {
        if(e.type === 'touchstart' || e.type === 'touchmove'){
            pos.x = e.touches[0].clientX-imgCreate.offsetLeft;
            pos.y = e.touches[0].clientY-imgCreate.offsetTop;
        } else if (e.type === 'mousedown' || e.type === 'mousemove' || e.type==='mouseenter') {
            pos.x = e.clientX-imgCreate.offsetLeft;
            pos.y = e.clientY-imgCreate.offsetTop;
        }

    }

    /**
     * Resizes the canvas
     */
    function resize() {
        canvas.width = imgCreate.clientWidth;
        canvas.height = imgCreate.clientHeight;
        loaded = false;
    }

    /**
     * Draws a line
     */
    function draw(e) {
        if (e.type === 'mousedown' || e.type === 'mousemove' || e.type==='mouseenter') {
            if (e.buttons !== 1) return;
        }
        if(draggedNow === false) {
        ctx.beginPath();

        ctx.lineWidth = 5;
        ctx.lineCap = 'round';
        ctx.strokeStyle = color;

        ctx.moveTo(pos.x, pos.y);
        setPosition(e);
        ctx.lineTo(pos.x, pos.y);

        ctx.stroke();
        ctx.save();
        }
    }

    /**
     * Starts the dragging
     */
    const start = (e) => {
        draggedNow = true;
        window.addEventListener('touchmove', divMove, true);
        window.addEventListener('mousemove', divMove, true);
    };

    /**
     * Ends the dragging
     */
    const end = () => {
        draggedNow = false;
        window.removeEventListener('mousemove', divMove, true);
        window.removeEventListener('touchmove', divMove, true);
    }
    /**
     * If the div (textfield) is moved
     */
    function divMove(e){
        let div = textfield.bind;
        div.style.position = 'absolute';
        if(e.type === 'touchmove'){
            div.style.top = e.touches[0].clientY - imgCreate.offsetTop - 50 + 'px';
            div.style.left = e.touches[0].clientX - imgCreate.offsetLeft + 'px';
        } else if (e.type === 'mousemove') {
            div.style.top = e.clientY - imgCreate.offsetTop - 50 + 'px';
            div.style.left = e.clientX - imgCreate.offsetLeft + 'px';
        }
    }

    /**
     * Generate the image
     */
    export function show() {
        if(text && textfield.textValue.length > 0) {
            let width = textfield.bind.clientWidth;
            let height = textfield.bind.clientHeight;
            let offsetLeft = textfield.bind.offsetLeft;
            let offsetTop = textfield.bind.offsetTop;
            ctx.fillStyle = color;
            ctx.fillRect(offsetLeft, offsetTop, width, height);
            ctx.fillStyle = '#ffffff';
            ctx.font = "24px Arial";
            ctx.textBaseline = "middle";
            ctx.fillText(textfield.textValue, offsetLeft+5, offsetTop+(height/2));
        }

        prop.value = canvas.toDataURL("image/jpeg");
    }
</script>
<svelte:window on:mouseup={end}
               on:touchend={end}/>
<div id="create-image"
     bind:this={imgCreate}
     on:mousemove={draw}
     on:touchmove={draw}
     on:mousedown={setPosition}
     on:mouseenter={setPosition}
     on:touchstart={setPosition}
     style="width:100%;height:100%;overflow:hidden;position: relative">
    {#if text}
        <span contenteditable="true" role="textbox"
              placeholder="Texteingabe"
              id="jodel-textf"
              name="text"
              on:mousedown={start}
              on:touchstart={start}
              bind:textContent={textfield.textValue}
              bind:this={textfield.bind}
              style="position:absolute;margin-top:50px;background-color: {color}">
        </span>
    {/if}
    <canvas bind:this={canvas} on:resize={resize}></canvas>
</div>