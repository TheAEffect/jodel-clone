<script>
	import {axiosAPI} from "../../apis/AxiosQuarkus";
	import Joi from 'joi';
	import Icon from 'svelte-awesome';
	import {faTint, faTimes, faCamera, faFont, faPaperPlane} from '@fortawesome/free-solid-svg-icons'
	import {storeGeo} from '../../utils/store.js';
	import Comment from './Comment.svelte'
	import { fly } from 'svelte/transition';
	import Bar from "../Bar.svelte";
	import Image from "../optionals/Image.svelte";
	import {afterUpdate, tick} from "svelte";

	export let display=undefined;
	export let image=undefined;
	export let displayText;
	export let comments;
	export let postId;
	export let color;
	export let step = {
		text: null,
		optional: {
			data: undefined
		},
		valid: undefined
	}
	let  fileinput;

	/**
	 * Validation schema for comment data
	 * @type {Joi.ObjectSchema<any>}
	 */
	const schema = Joi.object({
		text: Joi.string().min(1).required(),
	});

	/**
	 * Validation schema for image data
	 * @type {Joi.ObjectSchema<any>}
	 */
	const imageSchema = Joi.object({
		value: Joi.string().required(),
		valid: Joi.boolean().invalid(false)
	})

	/**
	 * toggles optionals (Image, undefined)
	 * @param option
	 */
	const toggleOptional = (option) => {
		step.optional = step.optional?.type === option ? {data:undefined}: step.optional = {data:undefined, type: option}
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
			step.optional = {...step.optional, data:{value: e.target.result, valid: true}}
		};
	}

	/**
	 * If input is done, check for valid schema
	 */
	$: {
		step.valid = undefined;
		if(step.optional?.type !== Image && schema.validate(
				{text: step.text}).error) {
			step.valid = false;
		}


		if(Image === step.optional?.type) {
			if(imageSchema.validate(step.optional?.data).error) {
				step.valid = false;
			}
		}

		if(step.valid === undefined) {
			step.valid = true;
		}
	}

	/**
	 * Prepare to upload the data & send it afterwards
	 */
	const uploadComment = () => {
		if (!step.valid) return;

		const isImage = step.optional?.type === Image || step.optional?.type === "Image";
		if (isImage && image?.show) {
			image.show();
		}

		const payload = {
			text: step.text,
			color: step.color,
			longitude: $storeGeo?.longitude,
			latitude: $storeGeo?.latitude,
			city: $storeGeo?.city,
			optional: isImage ? { type: "Image", data: step.optional?.data } : {}
		};

		step.optional = { data: undefined };

		axiosAPI().put(`/comment/${postId}`, payload)
				.then(async (res) => {
					if (res.status === 201) {
						comments = [...comments, res.data];
						step.text = "";
						await tick();
						window.scrollTo(0, document.getElementById("main").scrollHeight);
					}
				});
	};

	/**
	 * Deletes comment with given id
	 * @param commentId
	 */
	export let deleteComment = (commentId) => {
		axiosAPI().delete('/comment/' + commentId)
				.then((res) => {
					comments = comments.filter(el => el.id !== commentId);
				});
	};

</script>
<div class="comments">
	<div>
		<hr/>
	</div>
	<div>
		{#each comments as comment, i}
			<Comment deleteComment={deleteComment}
					 comment={comment}/>
		{/each}
		<Bar bottom>
			{#if !step.optional?.type}
				<div style="padding: 10px 0 10px 10px;">
					<input placeholder="#GoodVibesOnly"
						   style="width:100%;border:none;border-bottom:1px solid {color}"
						   bind:value={step.text}/>
				</div>
			{/if}
			<div class="post-settings">
				{#if display === "color"}
					<div style="display:flex; justify-content: end">
						<div style="display:flex; justify-content: end"
							 in:fly="{{ x: 100, duration: 1000 }}">
							<span class="dot"
								  style="background-color:#FF9908"
								  on:click={()=>{ color = '#FF9908';display=null }}>
							</span>
							<span class="dot"
								  style="background-color:#FFBA00"
								  on:click={()=>{ color = '#FFBA00';display=null }}>
							</span>
							<span class="dot"
								  style="background-color:#DD5F5F"
								  on:click={()=>{ color = '#DD5F5F';display=null }}>
							</span>
							<span class="dot"
								  style="background-color:#06A3CB"
								  on:click={()=>{ color = '#06A3CB';display=null }}>
							</span>
							<span class="dot"
								  style="background-color:#8ABDB0"
								  on:click={()=>{ color = '#8ABDB0';display=null }}>
							</span>
							<span class="dot"
								  style="background-color:#9EC41C"
								  on:click={()=>{ color = '#9EC41C';display=null }}>
							</span>
						</div>
						<div>
                                <span class="greydot"
									  on:click={()=>{display=null}}>
                                    <Icon style="font-size:0.8em;color:#fff"
										  data={faTimes}/>
                                </span>
						</div>
					</div>
				{:else if !display}
					{#if step.optional?.type === Image}
						 <span class="greydot" on:click={()=>{displayText = !displayText}}>
							 <Icon style="font-size:0.8em;color:#fff" data={faFont}/>
						 </span>
						 <span class="greydot" on:click={()=>{display="color"}}>
							 <Icon style="font-size:0.8em;color:#fff" data={faTint}/>
						 </span>
					{/if}
					{#if !step.text}
						<span class="greydot"
							  on:click={()=>{
                                    if(step.optional?.type) {
                                        step.optional.type = undefined;
                                        displayText = false;
                                    } else {
                                        fileinput.click()
                                    }
                                }}>
							<Icon style="font-size:0.8em;color:#fff"
								  data={faCamera}/>
						</span>
						<span><input
								style="display:none"
								type="file"
								accept=".jpg, .jpeg, .png"
								on:change={(e)=>onFileSelected(e)} bind:this={fileinput} >
						</span>
					{/if}
					{#if step.text || step.optional?.type}
						<span class="greydot"
							  style="background-color:{step.color}"
							  on:click={uploadComment}
							  class:not-valid={!step.valid}>
							<Icon style="font-size:0.8em;color:#fff"
								  data={faPaperPlane}/>
						</span>
					{/if}
				{/if}
			</div>
		</Bar>
	</div>
</div>

