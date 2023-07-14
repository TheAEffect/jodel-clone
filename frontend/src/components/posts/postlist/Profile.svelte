<script>
    import {axiosAPI} from "../../../apis/AxiosQuarkus.js";
    import Textfield from '@smui/textfield';
    import Icon from 'svelte-awesome';
    import {Button} from 'sveltestrap';
    import {ListGroup, ListGroupItem} from 'sveltestrap';
    import {storeSection} from "../../../utils/store";
    import {Alert, Toast} from 'sveltestrap';
    import {
        faArrowLeft
    } from '@fortawesome/free-solid-svg-icons'
    import Bar from "../../Bar.svelte";
    import {useAuth} from "../../../contexts/AuthContext";
    import Joi from "joi";
    import PostList from "../PostList.svelte";

    let isOpen = true;
    export let posts;
    let date = new Date().getTime();
    let menu;
    const authContext = useAuth();
    export let rndColor = undefined;
    const state = {
        newPw: null,
        newPwRepeat: null,
        password: null,
        valid: undefined,
    };

    let messages = {
        error: undefined,
        success: undefined
    }

    /**
     * Validation schema for register data
     * @type {Joi.ObjectSchema<any>}
     */
    const registerSchema = Joi.object({
        newPw: Joi.string().required(),
        newPwRepeat: Joi.string().required(),
        password: Joi.string().required()
    });

    /**
     * logs the user out
     */
    const logout = () => {
        authContext.logout();
    }

    /**
     * If input is done, check for valid schema
     */
    $: {
        state.valid = undefined;
        if (registerSchema.validate({
            newPw: state.newPw,
            newPwRepeat: state.newPwRepeat,
            password: state.password
        }).error) {
            state.valid = false;
        }

        if (state.newPw !== state.newPwRepeat) {
            state.valid = false;
        }

        if (state.valid === undefined) {
            state.valid = true;
        }
    }

    /**
     * handle the submit for the eddited data
     */
    const handleEditSubmit = () => {
        authContext.editUser({
            'newPassword': state.newPw,
            'newPassword2': state.newPwRepeat,
            'oldPassword': state.password
        })
            .then(data => {
                messages = {success: data};
                state.newPw = null;
                state.newPwRepeat = null;
                state.password = null;
            })
            .catch(err => {
                if (err.response) {
                    messages = {error: err.response.data};
                }
            });
    }

    /**
     * deletes the account of the user
     */
    const deleteAccount = () => {
        authContext.deleteUser();
    }

    /**
     * Gets own Posts/Votes/Comments done
     * @param by the type to get the posts by ("post", "comment", "vote")
     */
    const handleMinePostsby = (by) => {
        axiosAPI().get('/post/mine?postsby=' + by)
            .then((res) => {
                if (res.status === 200) {
                    posts = res.data;
                    storeSection.set({'name': 'profile','displayPosts':true});
                }
            });
    }
</script>
<div>
    {#if !menu}
        <ListGroup style="margin:20px;">
                <ListGroupItem on:click={()=>{handleMinePostsby('post')}} tag="button">Meine Jodel</ListGroupItem>
                <ListGroupItem on:click={()=>{handleMinePostsby('comment')}} tag="button">Meine Antworten</ListGroupItem>
                <ListGroupItem on:click={()=>{handleMinePostsby('vote')}} tag="button">Meine Votes</ListGroupItem>
                <ListGroupItem on:click={()=>{menu={name:"settings"}}} tag="button">Mehr</ListGroupItem>
        </ListGroup>
    {:else if menu?.name === "settings"}
        <Bar top>
            <span on:click={()=>{menu=undefined}}>
                <Icon class="jodel-post-arrow-icon"
                      data={faArrowLeft}/>
            </span>
        </Bar>
        <ListGroup style="margin:20px;">
            <ListGroupItem tag="button" on:click={()=>{menu.name="editPw"}}>Account bearbeiten</ListGroupItem>
            <ListGroupItem tag="button" on:click={deleteAccount}>Mein Account löschen</ListGroupItem>
            <ListGroupItem tag="button" on:click={logout}>Logout</ListGroupItem>
        </ListGroup>
    {:else if menu?.name === "editPw"}
        <Bar top>
            <span on:click={()=>{menu.name="settings"}}>
                <Icon class="jodel-post-arrow-icon"
                      data={faArrowLeft}/>
            </span>
        </Bar>
        <div class="edit">
            <div style="height:100px;padding:20px;">
                {#if messages?.error || messages?.success}
                    <Toast {isOpen}
                           autohide
                           body
                           on:close={() => (isOpen = false)}
                    >
                        <Alert color={messages?.error? "danger" : "success"}>
                            {#if messages?.error}
                                {messages?.error}
                            {:else}
                                {messages?.success}
                            {/if}
                        </Alert>
                    </Toast>
                {/if}
            </div>
            <div style="width: 100%">
                <Textfield
                        id="newPassword"
                        type="text"
                        bind:value={state.newPw}
                        label="Neues Passwort">
                    <Icon class="material-icons" slot="trailingIcon">password</Icon>
                </Textfield>
            </div>
            <div style="width: 100%">
                <Textfield
                        type="text"
                        bind:value={state.newPwRepeat}
                        label="Passwort wiederholen">
                    <Icon class="material-icons" slot="trailingIcon">password</Icon>
                </Textfield>
            </div>
            <div style="width: 100%">
                <Textfield
                        type="password"
                        bind:value={state.password}
                        label="Altes Passwort">
                    <Icon class="material-icons" slot="trailingIcon">password</Icon>
                </Textfield>
            </div>
            <div style="width: 100%">
                <Button disabled="{!state.valid}" on:click={handleEditSubmit}>Speichern</Button>
            </div>
        </div>
    {:else if menu.name === "list"}
        <PostList bind:posts={state.posts} color={rndColor} />
    {/if}
</div>

<style>
    :global(.btn-secondary) {
        color: #fff;
        background-color: var(--mdc-theme-primary) !important;
        border-color:var(--mdc-theme-primary) !important;
    }

    :global(.mdc-floating-label) {
        color:var(--mdc-theme-primary) !important;
    }

    :global(.btn-secondary:disabled) {
        color: #fff;
        background-color: var(--mdc-theme-primary) !important;
        border-color:var(--mdc-theme-primary) !important;
    }

    :global(.mdc-switch.mdc-switch--selected:enabled:active) {
        background: var(--mdc-theme-primary) !important;
    }

    :global(.mdc-switch__handle::after) {
        background: var(--mdc-theme-primary) !important;
    }

    :global(.mdc-switch__track::after) {
        background: #e0e0e0 !important;
    }
</style>