<script>
    import {convertTime, timeDiff} from '../../utils/convertTime';
    import {axiosAPI} from "../../apis/AxiosQuarkus.js";
    import {navigate} from "svelte-routing";
    import Vote from "../Vote.svelte";
    import CommentList from "../comments/CommentList.svelte";
    import Icon from 'svelte-awesome';
    import Image from "../optionals/Image.svelte"
    import ImageLayer from "../ImageLayer.svelte"
    import {
        faMapMarkerAlt,
        faTrash,
        faCommentAlt,
        faCrown,
        faArrowLeft,
        faPollH,
        faCheckCircle
    } from '@fortawesome/free-solid-svg-icons'
    import Bar from "../Bar.svelte";
    export let post;
    export let singlePost=true;
    export let surveyOptionActive=undefined;
    let date = new Date().getTime();
    let displayText = false;
    let imgPopover;
    let imageComponent;
    let step = {
        color: post?.color,
        text: null,
        optional: {
            data: undefined
        },
        valid: undefined
    }
    export let deletePost = (postId) => {
        axiosAPI().delete('/post/' + postId)
            .then((res) => {
                location.href = '/posts';
            });
    };

    /**
     * Refreshes state if post.image has loaded
     */
    $: bgImage = "data:image/png;base64," + post.image;

    /**
     * Displays Image of pressed
     */
    const pressed = () => {
        imgPopover = ImageLayer
    }

    /**
     * Sets the state of the option as activ
     * If is clicked again save the option show the result
     */
    const setOptionActive = (surveyId) => {
        if (post.survey_votes == null) {
            if (surveyOptionActive === surveyId) {
                showSurveyResults(surveyId, post.id)
            } else {
                surveyOptionActive = surveyId;
            }
        }
    }

    /**
     * Show the result of the survey
     */
    const showSurveyResults = () => {
        axiosAPI().put('/survey/' + post.id, {id: surveyOptionActive})
            .then((res) => {
                post = res.data;
            });
    }

    /**
     * Hides Image if no longer pressed
     */
    const released = () => {
        imgPopover = undefined;
    }

</script>
<svelte:component this={imgPopover}
                  post={post}
                  bgImage={bgImage}
                  released={released}/>
<div class={step.optional?.type === Image ? "create img":""}
     class:container={singlePost}
     class:main={singlePost}
     class:item={!singlePost}
     id="main"
     style="background-color: {post.color}">
    {#if singlePost}
        <Bar top>
            <span on:click={()=>{navigate("/posts", { replace: true });}}>
                <Icon class="jodel-post-arrow-icon"
                      data={faArrowLeft}/>
            </span>
        </Bar>
    {/if}
    <svelte:component this={step.optional?.type}
                      bind:this={imageComponent}
                      bind:prop={step.optional.data}
                      bind:text={displayText}
                      color={step.color}/>
    {#if !step.optional?.type}
    <div on:mousedown={pressed}
         on:touchstart={pressed}
         style={post.type==="IMAGE" ? `background-image: url('${bgImage}');`:``}>
    <div class="jodel-post jodel-card-style blur"
         on:click={()=>navigate('/post/'+post.id)}
         class:img={post.type==="IMAGE"}>
            <div class="headerLine">
                <div>
                    {#if singlePost}
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
                        <span class="small paddingRight">OJ</span>
                    {/if}
                    <span class="small" style="display:inline-block;padding:2px;background: rgba(0,0,0,0.5);border-radius: 5px;line-height: 1em;">
                        @{post.channel?.name.toLowerCase()}
                    </span>
                    <span class="small">
                        <Icon data={faMapMarkerAlt} /> {post.city}
                    </span>
                    <span class="vertical"> &middot;</span>
                    <span class="small"> {convertTime(post.postedAt)}</span>
                    {#if post.type === "SURVEY"}
                        <span class="vertical"> &middot;</span>
                        <span class="small"> {timeDiff(post.postedAt)}</span>
                    {/if}
                </div>
                <div>
                    {#if post.yours}
                        <span class="deleteButton" on:click={(e)=>{e.stopPropagation();deletePost(post.id)}}>
                            <Icon data={faTrash}/>
                        </span>
                    {/if}
                </div>
            </div>
            <div class="grid">
                {#if post.type === "IMAGE"}
                    <div style="text-align: center;width:100%">Gedrückt halten</div>
                {:else}
                    <div style="width: 100%;">
                        <div>{post.text}</div>
                        <div style="padding-top:15px">
                            {post.hashtag? post.hashtag:''}
                        </div>
                        {#if post.type === "LINK"}
                            <div>
                                <a href="https://{post.link.replace('https://','').replace('http://','')}">{post.link}</a>
                            </div>
                        {:else if post.type === "SURVEY"}
                            <div class="post-survey">
                                {#if post.survey_votes}
                                    {#each post.surveys || [] as survey, i}
                                        <div on:click={(e)=>{e.stopPropagation()}}
                                             class="finished post-survey-option">
                                            <div style="position: relative;display: flex;justify-content: space-between;">
                                                <span style="position: relative;z-index: 2; ">
                                                    {survey.option}
                                                </span>
                                                <span style="position: relative;z-index: 2; ">
                                                    {#if post.vote_id === survey.id}
                                                        <Icon data={faCheckCircle}/>
                                                    {/if}
                                                    {parseFloat(100 / post.survey_votes * survey.votes).toFixed(1)}%
                                                </span>
                                                <div style="position:absolute;z-index:1;top:0;left:0;right:0;bottom:0;background: rgba(255,255,255,0.5);width:{100 / post.survey_votes * survey.votes}%"></div>
                                            </div>
                                        </div>
                                    {/each}
                                {:else}
                                    {#each post.surveys || [] as survey, i}
                                        <div on:click={(e)=>{e.stopPropagation();setOptionActive(survey.id)}}
                                             class:active-option={surveyOptionActive===survey.id}
                                             class:post-survey-option={surveyOptionActive!==survey.id}>
                                            <span>
                                                {survey.option}
                                            </span>
                                        </div>
                                    {/each}
                                    {#if post.surveys?.filter(survey => survey.id === surveyOptionActive).length > 0}
                                        <div class="show-result"
                                             on:click={()=>{showSurveyResults()}}>
                                            <span><Icon data={faPollH}/></span>Ergebnisse anzeigen
                                        </div>
                                    {/if}
                                {/if}
                            </div>
                        {/if}
                    </div>
                {/if}
                <Vote bind:post={post}
                      postcomment="post"/>
            </div>
            <div class="post-bottom">
                <span class="small">
                    <Icon data={faCommentAlt}/> {post.comment_number}
                </span>
                {#if post.survey_votes}
                    <span class="small">{post.survey_votes} {post.survey_votes > 1? ' Stimmen': ' Stimme'}</span>
                {/if}
            </div>
    </div>
    </div>
    {/if}
    {#if singlePost}
        <CommentList bind:image={imageComponent}
                     bind:displayText={displayText}
                     bind:step={step}
                     comments={post.comments}
                     postId={post.id}
                     color={post.color}/>
    {/if}
</div>
