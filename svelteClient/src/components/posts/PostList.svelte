<script>
    import Post from "./Post.svelte";
    import {ListGroup, ListGroupItem} from 'sveltestrap';
    import {faPlus, faArrowLeft, faClock, faCommentAlt, faChevronUp, faAt} from '@fortawesome/free-solid-svg-icons'
    import Switch from '@smui/switch';
    import Bar from "../Bar.svelte";
    import {useAuth} from "../../contexts/AuthContext";
    import { slide } from 'svelte/transition';
    import Icon from 'svelte-awesome';
    import { Icon as Ic } from 'sveltestrap';
    import {storeGeo, storeChannel, storeOrder, storeSection, storeUser} from "../../utils/store";
    import {axiosAPI} from "../../apis/AxiosQuarkus";
    import {
        faMapMarkerAlt,
    } from '@fortawesome/free-solid-svg-icons'
    import Profile from "./postlist/Profile.svelte";
    import {navigate} from "svelte-routing";

    export let posts;
    export let color;
    export let setOrderAndFetch;
    export let createPost;
    let channels;
    let displayLocationChange=false;
    let surveyOptionActive;
    const authContext = useAuth();

    /**
     * loads the channels & displays it
     */
    const loadChannels = () => {
        if ($storeSection !== "channel") {
            axiosAPI().get('/channel')
                .then((res) => {
                    if (res.status === 200) {
                        channels = res.data;
                    }
                });
            storeSection.set("channel");
        }
    }

    /**
     * toggles the autoDistance (true <-> false)
     */
    const toggleAutoDistance = () => {
        authContext.editUser({
            'autoDistance': !$storeUser?.autoDistance
        })
            .then(res => {
                storeUser.set({...$storeUser, ...res.data});
            })
    }

    /**
     * handles the click on Location in top Bar
     */
    const clickLocation = () => {
        if($storeSection) {
            storeSection.set(undefined);
        } else {
            displayLocationChange = !displayLocationChange;
        }
    }

    /**
     * deletes a post
     */
    const deletePost = (postId) => {
        axiosAPI().delete('/post/' + postId)
            .then((res) => {
                navigate('/posts')
                posts = posts.filter(el => el.id !== postId);
            });
    }
</script>
<div class="container main postlist">
    <Bar top>
        {#if !$storeChannel?.id}
            <div class="top-selectable"
                 class:top-selected={$storeSection==="channel"}
                 style="color:{color};"
                 on:click={()=>{
                     storeChannel.set(undefined);
                     loadChannels();
                 }}>
                <Icon data={faAt} />
            </div>
            <div class="top-selectable"
                 class:top-selected={!$storeSection}
                 style="line-height:{$storeChannel?.name?'24px':'48px'};color:{color};"
                 on:click={clickLocation}>
                <Icon data={faMapMarkerAlt} /> {$storeGeo.city}
            </div>
            <div class="top-selectable"
                 class:top-selected={$storeSection?.name==="profile"}
                 style="color:{color};"
                 on:click={()=>{
                     storeSection.set({name:"profile"});
                 }}>
                <div style="display: flex;align-items: center;justify-content: center;flex-direction: column;height: 48px">
                    <div style="line-height:12px;font-weight: bold;">{Math.sign(parseInt($storeUser?.karma))>=0?`+${$storeUser?.karma}`:`${$storeUser?.karma}`}</div>
                    <div style="line-height:12px;font-size:8px;">MEIN KARMA</div>
                </div>
            </div>
        {:else}
            <div style="width:100%"
                 on:click={()=>{
                     storeChannel.set(undefined);
                     setOrderAndFetch()}}>
                <Icon class="jodel-post-arrow-icon"
                    data={faArrowLeft}/>
            </div>
            <div class="top-selectable"
                 class:top-selected={!$storeSection}
                 style="line-height:{$storeChannel.name?'24px':'48px'};color:{color};"
                 on:click={()=>{storeSection.set(undefined)}}>
                <Icon data={faMapMarkerAlt} /> {$storeGeo.city}
                <br>@{$storeChannel.name}
            </div>
            <div style="width:100%"></div>
        {/if}
    </Bar>
    {#if $storeSection === "channel"}
        <div class="channels">
        <div style="color:#333;margin:0 0 10px 0">MEINE CHANNELS</div>
        <ListGroup>
            {#each channels || [] as channel, i}
                <ListGroupItem tag="button"
                               on:click={()=>{
                                   storeChannel.set({id: channel.id, name: channel.name.toLowerCase()});
                                   storeSection.set(undefined);
                                   setOrderAndFetch();
                               }}
                               style={$storeChannel?.id===channel.id? `color:#fff;background-color: ${color};border-color:${color}`:
                                                                `background-color: #fff;border-color:#fff`}>
                    <div>{channel.symbol}</div>
                    <div>
                        <div style={$storeChannel?.id===channel.id? `font-weight:bold`:``}>{channel.name}</div>
                        <div><small style={$storeChannel?.id===channel.id? `color:#fff`:``}>{channel.info}</small></div>
                    </div>
                </ListGroupItem>
            {/each}
        </ListGroup>
        </div>
    {:else if $storeSection?.name === "profile" && !$storeSection?.displayPosts}
        <Profile bind:posts={posts}/>
    {:else}
        {#if displayLocationChange}
            <div class="location-change" transition:slide>
                <div>
                    <div style="text-align: center;">Orte</div>
                    <div style="display: flex;align-content: center">
                        <div style="align-self: center;">
                            <span style="width:30px; height:30px;color:#fff;display:flex;justify-content:center; align-items:center;border-radius: 50%;background: {color}">
                                <Icon data="{faMapMarkerAlt}"/></span>
                        </div>
                        <div style="font-size: 0.8em; margin-left:20px;line-height: 1.1em;">
                            <div>{$storeGeo.city}</div>
                            <div>Aktueller Standort</div>
                        </div>
                    </div>
                    <div style="text-align: center;padding-top:20px">Reichweite</div>
                    <div style="dont-size:0.8em;display: flex;justify-content: space-between">
                        <span>Dynamischer Radius</span>
                        <span>
                            {#if $storeUser?.autoDistance}
                                Active <Ic name="wifi" />
                            {:else}
                                Inactive <Ic name="wifi-off" />
                            {/if}
                        </span>
                    </div>
                    <div on:click|preventDefault={toggleAutoDistance} style="font-size:0.8em;display: flex;justify-content: space-between">
                        <span>Vergrößere den Radius angezeigter Jodel in Regionnen mit wenig Aktivität automatisch</span>
                        <span>
                            <Switch checked={$storeUser.autoDistance} icons={false}/>
                        </span>
                    </div>
                </div>
            </div>
        {/if}
        {#each posts as post, i}
            <Post bind:surveyOptionActive
                  deletePost={deletePost}
                  post={post}
                  singlePost={false}/>
        {/each}
        {#if $storeSection?.name !== "profile"}
            <div class="flex">
                <div on:click={() => {createPost = true}}
                     class="icon-wrapper">
                    <Icon class="custom-icon"
                        data={faPlus}/>
                </div>
            </div>
        {/if}
    {/if}
    {#if $storeSection?.displayPosts}
        <Bar top>
            <span on:click={()=>{storeSection.set(undefined);setOrderAndFetch()}}>
                <Icon class="jodel-post-arrow-icon"
                      data={faArrowLeft}/>
            </span>
        </Bar>
    {/if}
    {#if !$storeSection?.name}
        <Bar bottom scale>
            <div class="sort-posts"
                 style="color: {!$storeOrder?color:''}"
                 on:click={()=>{storeOrder.set(undefined);setOrderAndFetch()}}>
                <Icon data={faClock}/>
            </div>
            <div class="sort-posts"
                 style="color:{$storeOrder===2?color:''};border-left: 1px solid #808080; border-right: 1px solid #808080"
                 on:click={()=>{storeOrder.set(2);setOrderAndFetch()}}>
                <Icon data={faCommentAlt}/>
            </div>
            <div class="sort-posts"
                 style="color:{$storeOrder===3?color:''}"
                 on:click={()=>{storeOrder.set(3);setOrderAndFetch()}}>
                <Icon data={faChevronUp}/>
            </div>
        </Bar>
    {/if}
</div>