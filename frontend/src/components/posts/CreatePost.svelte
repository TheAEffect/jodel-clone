<script>
    import { ListGroup, ListGroupItem } from 'sveltestrap';
    import Joi from 'joi';
    import {axiosAPI} from "../../apis/AxiosQuarkus";
    import Icon from 'svelte-awesome';
    import {faList, faArrowLeft, faTint, faTimes, faLink, faCamera, faFont, faPaperPlane} from '@fortawesome/free-solid-svg-icons'
    import Bar from "../Bar.svelte";
    import { fly } from 'svelte/transition';
    import Survey from "../optionals/Survey.svelte"
    import Link from "../optionals/Link.svelte"
    import Image from "../optionals/Image.svelte"
    import {storeGeo} from "../../utils/store";

    export let createPost;
    export let posts;
    export let rndColor;
    let display = null;
    let displayText = false;
    let fileinput;
    let child;
    let placeholder;
    let steps = {
        step1: {
            active: true,
            color: rndColor,
            text: null,
            hashtag:null,
            optional: {
                data: undefined
            },
            valid: undefined
        },
        step2: {
            active: false,
            selected: undefined,
            valid: undefined
        }
    }

    /**
     * Validation schema for post data
     * @type {Joi.ObjectSchema<any>}
     */
    const schema = Joi.object({
        text: Joi.string().min(1).required(),
        color: Joi.string().pattern(new RegExp('^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$')).required,
        hashtag: Joi.string().min(2).optional,
    });

    /**
     * Validation schema for survey data
     * @type {Joi.ObjectSchema<any>}
     */
    const surveySchema = Joi.array().items(Joi.object({
        value: Joi.string(),
        removable: Joi.bool().optional()
    })).min(2).max(4)

    /**
     * Validation schema for link & image data
     * @type {Joi.ObjectSchema<any>}
     */
    const linkImageSchema = Joi.object({
        value: Joi.string().required(),
        valid: Joi.boolean().invalid(false)
    })

    /**
     *  Prevents to insert antyhing else than text/plain
     *  @param e event object
     */
    const handlePaste = (e) => {
        e.preventDefault();

        var text = (e.originalEvent || e).clipboardData.getData('text/plain');

        document.execCommand("insertHTML", false, text);

    };
    /**
     * Method to handle input changes
     * @param e event object
     */
    const handleChange = (e) => {
        e.target.style.height = "auto";
        e.target.style.height = (e.target.scrollHeight) + "px";
        if(e.target.innerText === "\n") {
            e.target.innerText = '';
        }
    };


    /**
     * Method to handle hashtag changes
     * @param e event object
     */
    const handleHashtagChange = (e) => {
        if(e.which === 8) {
            if(steps.step1.hashtag === '#') {
                e.preventDefault();
                document.getElementById('jodel-text').focus();
            }
        }
    };

    /**
     * Method to handle hashtag focus
     */
    const handleHashtagFocus = () => {
        if(steps.step1.hashtag === null || steps.step1.hashtag === '') {
            steps.step1.hashtag = '#';
        }
    };

    /**
     * Method to handle hashtag focus out
     */
    const handleHashtagFocusOut = () => {
        if(steps.step1.hashtag === '#') {
            steps.step1.hashtag = '';
        }
    };

    /**
     * toggles optionals (Link, Image, Survey, undefined)
     * @param option
     */
    const toggleOptional = (option) => {
        steps.step1.optional = steps.step1.optional?.type === option ?
            {data:undefined}: steps.step1.optional = {data:undefined, type: option}
    }

    /**
     * Goes to step 2 & loads the channels
     */
    const gotoStep2 = () => {
        if(steps.step1.valid) {
            steps.step1.active = false;
            steps.step2.active = true;
        }
        loadChannels();
    }

    /**
     * loads the channels & displays it
     */
    const loadChannels = () => {
        if(steps.step1.optional?.type === Image) {
            child.show();
        }

        axiosAPI().get('/channel')
            .then((res) => {
                if (res.status === 200) {
                    steps.step2.channels = res.data;
                }
            });
    }

    /**
     * If input is done, check for valid schema
     */
    $: {
        steps.step1.valid = undefined;
        if(steps.step1.optional?.type !== Image && schema.validate(
            {text: steps.step1.text},
            {color: steps.step1.color},
            {hashtag: steps.step1.hashtag}).error) {
            steps.step1.valid = false;
        }

        if(steps.step1.optional?.type === Survey) {
            if(surveySchema.validate(steps.step1.optional?.data?.choices).error) {
                steps.step1.valid = false;
            }
        }

        if([Link,Image].includes(steps.step1.optional?.type)) {
            if(linkImageSchema.validate(steps.step1.optional?.data).error) {
                steps.step1.valid = false;
            }
        }

        if(steps.step1.valid === undefined) {
            steps.step1.valid = true;
        }
    }

    /**
     * Handles if a file has been uploaded
     * @param e
     */
    const onFileSelected =(e)=>{
        let image = e.target.files[0];
        let reader = new FileReader();
        reader.readAsDataURL(image);
        toggleOptional(Image);
        reader.onload = e => {
            steps.step1.optional = {...steps.step1.optional, data:{value: e.target.result, valid: true}}
        };
    }

    /**
     * Prepare to upload the data & send it afterwards
     */
    const uploadPost = () => {
        if(steps.step1.valid && steps.step2.valid) {
            let obj = steps.step1;
            obj = {...obj, longitude: $storeGeo?.longitude, latitude: $storeGeo?.latitude, city: $storeGeo?.city}
            delete obj.active;
            if(steps.step1.optional?.type === Survey) {
                delete obj.optional?.data?.addChoice;
                obj.optional.data = obj.optional?.data.choices.map( function(choice) {
                    return choice.value
                });
            }

            delete obj.valid;

            if(steps.step1.optional?.type !== undefined) {
                steps.step1.optional.type = steps.step1.optional?.type?.name?.replace('_1','');
            }

            obj = {...obj, channelid:steps.step2.selected}

            axiosAPI().put('/post', obj)
                .then((res) => {
                    if (res.status === 201) {
                        posts = [res.data, ...posts];
                        createPost = false;
                    }
                });
        }
    }
</script>
<div class="container main create" class:img={steps.step1.optional?.type === Image} style='color:#999;background-color: {steps.step1.active? steps.step1.color : steps.step2.active? "#f5f5f5":"#ddd"}'>
    <Bar top>
        {#if steps.step1.active}
             <span on:click={()=>{history.back()}}>
                <Icon class="jodel-post-arrow-icon" data={faArrowLeft}/>
            </span>
            <span on:click={gotoStep2} class={steps.step1.valid? 'valid':'not-valid'}>Weiter</span>
        {/if}
        {#if steps.step2.active}
             <span on:click={()=>{steps.step1.active = true;steps.step2 = {active: false, selected: undefined}}}>
                <Icon class="jodel-post-arrow-icon" data={faArrowLeft}/>
            </span>
            <div><span style="color:#000;padding-right:10px">Senden an...</span><span style="color:red">📍</span></div>
            <span></span>
        {/if}
    </Bar>
    <div class="jodel-post">
        {#if steps.step1.active}
            <form autocomplete="off">
                {#if steps.step1.optional?.type !== Image}
                    <div contenteditable="true"
                            placeholder={placeholder? placeholder : "Teile hier deine Gedanken und Erlebnisse mit den Menschen in deiner Umgbung"}
                            id="jodel-text"
                            bind:textContent={steps.step1.text}
                            on:input={handleChange}
                            on:paste={handlePaste}
                            name="text"
                    ></div>
                    <input
                            placeholder="#hashtaghinzufügen"
                            id="jodel-hashtag"
                            on:keydown={handleHashtagChange}
                            on:focusin={handleHashtagFocus}
                            on:focusout={handleHashtagFocusOut}
                            name="text"
                            maxlength="30"
                            bind:value={steps.step1.hashtag}
                    />
                {/if}
                <svelte:component this={steps.step1.optional?.type} bind:this={child} bind:prop={steps.step1.optional.data} color={steps.step1.color} bind:text={displayText}/>
                <Bar bottom color={steps.step1.color} overflow="hidden">
                    <div class="post-settings">
                        {#if display === "color"}
                            <div style="display:flex; justify-content: end">
                                <div style="display:flex; justify-content: end" in:fly="{{ x: 100, duration: 1000 }}">
                                <span class="dot" style="background-color:#FF9908" on:click={()=>{ steps.step1.color = '#FF9908';display=null }}></span>
                                <span class="dot" style="background-color:#FFBA00" on:click={()=>{ steps.step1.color = '#FFBA00';display=null }}></span>
                                <span class="dot" style="background-color:#DD5F5F" on:click={()=>{ steps.step1.color = '#DD5F5F';display=null }}></span>
                                <span class="dot" style="background-color:#06A3CB" on:click={()=>{ steps.step1.color = '#06A3CB';display=null }}></span>
                                <span class="dot" style="background-color:#8ABDB0" on:click={()=>{ steps.step1.color = '#8ABDB0';display=null }}></span>
                                <span class="dot" style="background-color:#9EC41C" on:click={()=>{ steps.step1.color = '#9EC41C';display=null }}></span>
                                </div>
                                <div>
                                <span class="greydot" on:click={()=>{display=null}}>
                                    <Icon style="font-size:0.8em;color:#fff" data={faTimes}/>
                                </span>
                                </div>
                            </div>
                        {:else if display === null}
                            {#if steps.step1.optional?.type === Image}
                                 <span class="greydot" on:click={()=>{displayText = !displayText}}>
                                    <Icon style="font-size:0.8em;color:#fff" data={faFont}/>
                                </span>
                            {/if}
                            <span class="greydot" on:click={()=>{display="color"}}>
                                <Icon style="font-size:0.8em;color:#fff" data={faTint}/>
                            </span>
                            {#if [undefined,Link].includes(steps.step1.optional?.type)}
                                <span class="greydot" on:click={()=>{
                                    toggleOptional(Link);
                                    steps.step1.hashtag = '#info';
                                    placeholder = placeholder? undefined : "Teile deine Gedanken über diesen Link!";
                                   }}>
                                    <Icon style="font-size:0.8em;color:#fff" data={faLink}/>
                                </span>
                            {/if}
                            {#if [undefined,Survey].includes(steps.step1.optional?.type)}
                                <span class="greydot" on:click={()=>{toggleOptional(Survey)}}>
                                    <Icon style="font-size:0.8em;color:#fff" data={faList}/>
                                </span>
                            {/if}
                            {#if [undefined,Image].includes(steps.step1.optional?.type)}
                                <span class="greydot" on:click={()=>{
                                    if(steps.step1.optional?.type) {
                                        steps.step1.optional.type = undefined;
                                    } else {
                                        fileinput.click()
                                    }
                                }}>
                                    <Icon style="font-size:0.8em;color:#fff" data={faCamera}/>
                                </span>
                                <span><input style="display:none" type="file" accept=".jpg, .jpeg, .png" on:change={(e)=>onFileSelected(e)} bind:this={fileinput} ></span>
                            {/if}
                        {/if}
                    </div>
                </Bar>
            </form>
        {:else if steps.step2.active}
            <div class="step2">
                <div style="color:#333;margin:0 0 10px 0">MEINE CHANNELS</div>
                <ListGroup>
                {#each steps.step2.channels || [] as channel, i}
                    <ListGroupItem tag="button" on:click={()=>{steps.step2.selected=channel.id; steps.step2.valid = true;}}
                                   style={steps.step2.selected===channel.id? `color:#fff;background-color: ${steps.step1.color};border-color:${steps.step1.color}`:
                                                                `background-color: #fff;border-color:#fff`}>
                        <div>{channel.symbol}</div>
                        <div>
                            <div style={steps.step2.selected===channel.id? `font-weight:bold`:``}>{channel.name}</div>
                            <div><small style={steps.step2.selected===channel.id? `color:#fff`:``}>{channel.info}</small></div>
                        </div>
                    </ListGroupItem>
                {/each}
                </ListGroup>
                <Bar bottom>
                    <div style="padding: 5px;height: 100%;">
                        <button class:active={steps.step2.valid} on:click={uploadPost} style="background-color: {steps.step2.selected?steps.step1.color:'#aaa'}">SENDEN <Icon data={faPaperPlane}/></button>
                    </div>
                </Bar>
            </div>
        {/if}
    </div>
</div>