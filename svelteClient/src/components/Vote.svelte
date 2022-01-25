<script>
    import Icon from 'svelte-awesome';
    import { faChevronUp, faChevronDown } from '@fortawesome/free-solid-svg-icons'
    import {axiosAPI} from "../apis/AxiosQuarkus.js";
    import {useAuth} from "../contexts/AuthContext";

    export let postcomment=undefined;
    export let post=undefined;
    let postcommentVote=undefined;

    /**
     * Handles the vote submit
     * @param event
     */
    function handleVote(event, vote) {
        event.stopPropagation();
        if(!postcommentVote) {
            axiosAPI().put("/vote", {postcomment: postcomment, id: post.id, vote: vote})
                .then(res => {
                    if (res.status === 200) {
                        if(post.votings) {
                            post.votings = [...post.votings, res.data];
                        } else {
                            post.votings = [res.data]
                        }
                        if(res.data?.type === "UP") {
                            post.votingValue++;
                        } else {
                            post.votingValue--;
                        }
                        useAuth().whoAmI();
                    }
                })
        }
    }

    /**
     * If input is done, check for valid schema
     */
    $: {
        postcommentVote = post?.votings?.find(vote => {
            return vote.yours === true;
        });
    }

</script>
{#if postcommentVote?.type}
    <div class="horizontale">
        <div class={postcommentVote?.type === "DOWN"? "voteDone":"vote"}>
            <Icon data={faChevronUp}/>
        </div>
        <div>{post.votingValue}</div>
        <div class={postcommentVote?.type === "UP"? "voteDone":"vote"}>
            <Icon data={faChevronDown}/>
        </div>
    </div>
{:else}
    <div class="horizontale open-vote">
        <div class="vote"
             on:click={(event) => handleVote(event, "up")}>
            <Icon data={faChevronUp}/>
        </div>
        <div>{post.votingValue}</div>
        <div class="vote"
             on:click={(event) => handleVote(event, "down")}>
            <Icon data={faChevronDown}/>
        </div>
    </div>
{/if}