<script>
	import {convertTime} from "../../utils/convertTime";
	import Vote from "../Vote.svelte";
	import Icon from 'svelte-awesome';
	import { faTrash, faMapMarkerAlt, faPaw, faCrown } from '@fortawesome/free-solid-svg-icons'
	import ImageLayer from "../ImageLayer.svelte";

	export let comment;
	export let deleteComment;
	let bgImage = "data:image/png;base64," + comment?.image;
	let imgPopover;

	/**
	 * Displays Image of pressed
	 */
	const pressed = (e) => {
		imgPopover = ImageLayer
	}

	/**
	 * Hides Image if no longer pressed
	 */
	const released = () => {
		imgPopover = undefined;
	}

</script>
<svelte:component this={imgPopover}
				  post={comment}
				  bgImage={bgImage}
				  released={released}/>
<div class="jodel-post jodel-comment"
	 on:mousedown={pressed}
	 on:touchstart={pressed}
	 style={comment.type==="IMAGE" ? `background-image: url('${bgImage}');`:``}>
	<div class="jodel-card-style blur img">
		<div class="headerLine">
			<div>
				<span class="small paddingRight">
					{#if comment.jodelNumber === 0}
						<div style="line-height: 14px;
									border-radius: 50%;
									width: 14px;
									font-size: 8px;
									height: 14px;
									text-align: center;
									display: inline-flex;
									vertical-align: text-bottom;
									align-items: center;
									justify-content: center;
									font-weight: bold;
									border: 1px solid #fff;
									margin-right: 2px;">
							<Icon data={faCrown}/>
						</div>
						OJ
					{:else}
						<div style="line-height: 14px;
									border-radius: 50%;
									width: 14px;
									height: 14px;
									font-size: 8px;
									text-align: center;
									display: inline-flex;
									vertical-align: text-bottom;
									align-items: center;
									justify-content: center;
									font-weight: bold;
									border: 1px solid #fff;
									margin-right: 2px;
									background-color: {comment.jodelNumberColor}">
							<Icon data={faPaw}/>
						</div>
						{comment.jodelNumber}
					{/if}
				</span>
				<span class="small">
					<Icon data={faMapMarkerAlt}/> {comment.city}
				</span>
				<span class="vertical"> &middot;</span>
				<span class="small"> {convertTime(comment.postedAt)}</span>
			</div>
			<div>
				{#if comment.yours}
					<span class="deleteButton" on:click={(e)=>{e.stopPropagation();deleteComment(comment.id)}}>
                            <Icon data={faTrash}/>
					</span>
				{/if}
			</div>
		</div>
		<div class="grid">
			{#if comment.type === "IMAGE"}
				<div style="text-align: center;width:100%">Gedrückt halten</div>
			{:else}
				<div>{comment.text}</div>
			{/if}
			<Vote postcomment="comment" bind:post={comment} />
		</div>
	</div>
</div>

