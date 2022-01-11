<script>
    import {useAuth} from "../contexts/AuthContext";
    import { navigate } from "svelte-routing";
    import Textfield from '@smui/textfield';
    import Icon from '@smui/textfield/icon';
    import Joi from 'joi';
    import { Alert, Toast } from 'sveltestrap';
    import { Router, Link } from "svelte-routing";
    import { Button } from 'sveltestrap';
    import {storeUser} from "../utils/store";
    import {onMount} from "svelte";

    export let register;
    let isOpen = true;
    const authContext = useAuth();
    const state = {
        password: null,
        passwordRepeat: null,
        email: null,
        loginButtonDisabled: true,
        valid: undefined
    };
    let messages = {
        error: undefined,
        success: undefined
    }

    /**
     * Runs when componenent loaded
     */
    onMount(() => {
        if($storeUser) {
            navigate('/posts');
        }
    });

    /**
     * Validation schema for login data
     * @type {Joi.ObjectSchema<any>}
     */
    const loginSchema = Joi.object({
        email: Joi.string().email({ minDomainSegments: 2, tlds: { allow: ['de'] } }).required(),
        password: Joi.string().required()
    });

    /**
     * Validation schema for register data
     * @type {Joi.ObjectSchema<any>}
     */
    const registerSchema = Joi.object({
        email: Joi.string().email({ minDomainSegments: 2, tlds: { allow: ['de'] } }).required(),
        password: Joi.string().required(),
        passwordRepeat: Joi.string().required()
    });

    /**
     * If input is done, check for valid schema
     */
    $: {
        if(!register) {
            state.valid = undefined;
            if(loginSchema.validate({
                email: state.email,
                password: state.password
            }).error) {
                state.valid = false;
            }

            if(state.valid === undefined) {
                state.valid = true;
            }
        } else {
            state.valid = undefined;
            if(registerSchema.validate({
                email: state.email,
                password: state.password,
                passwordRepeat: state.password
            }).error) {
                state.valid = false;
            }

            if(state.passwordRepeat !== state.password) {
                state.valid = false;
            }

            if(state.valid === undefined) {
                state.valid = true;
            }
        }
    }

    /**
     * Handles the login submit
     * @param event
     */
    const handleLoginSubmit = (event) => {
        event.preventDefault();

        authContext.login(state.email, state.password)
            .then(data => {
                navigate("/posts", { replace: true });
            })
            .catch(err => {
                if (err.response) {
                    messages = {error : err.response.data};
                    isOpen = true;
                }
            });
    }

    /**
     * Resets all input fields
     */
    const resetFields = () => {
        state.password = null;
        state.passwordRepeat = null;
        state.email = null;
        state.loginButtonDisabled = true;
        state.valid = undefined;
    }

    /**
     * Handles the register submit
     * @param event
     */
    const handleRegisterSubmit = (event) => {
        event.preventDefault();

        authContext.register(state.email, state.password, state.passwordRepeat)
            .then(data => {
                resetFields();
                messages = {success : data};
                register = false;
                isOpen = true;
            })
            .catch(err => {
                if (err.response) {
                    messages = {error : err.response.data};
                    isOpen = true;
                }
            });
    }
</script>
<div class="container">
    <div style="height:100vh;display: flex; align-items: center;justify-content: center">
        <div style="display:block;width: 50ch;">
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
        {#if !register}
            <div class="loginbox center">
                <div class="columns margins">
                    <h3>Anmelden</h3>
                    <div>
                        <Textfield
                                id="email"
                                type="email"
                                bind:value={state.email}
                                label="Email">
                            <Icon class="material-icons" slot="trailingIcon">person</Icon>
                        </Textfield>
                    </div>
                    <div>
                        <Textfield
                                type="password"
                                bind:value={state.password}
                                label="Passwort">
                            <Icon class="material-icons" slot="trailingIcon">password</Icon>
                        </Textfield>
                    </div>
                    <div class="buttonLeftAlign">
                        <Button disabled="{!state.valid}" on:click={(e) => handleLoginSubmit(e)}>Login</Button>
                    </div>
                    <div style="text-align: right;">
                        <small>
                            <Router>
                                <nav>
                                    Noch kein Account? <Link to="/register">Jetzt registrieren</Link>
                                </nav>
                            </Router>
                        </small>
                    </div>
                </div>
            </div>
        {:else}
            <div class="loginbox center">
                <div class="columns margins">
                    <h3>Registrieren</h3>
                    <div>
                        <Textfield
                                id="email"
                                type="email"
                                bind:value={state.email}
                                label="Email">
                            <Icon class="material-icons" slot="trailingIcon">person</Icon>
                        </Textfield>
                    </div>
                    <div>
                        <Textfield
                                type="password"
                                bind:value={state.password}
                                label="Passwort">
                            <Icon class="material-icons" slot="trailingIcon">password</Icon>
                        </Textfield>
                    </div>
                    <div>
                        <Textfield
                                type="password"
                                bind:value={state.passwordRepeat}
                                label="Passwort wiederholen">
                            <Icon class="material-icons" slot="trailingIcon">password</Icon>
                        </Textfield>
                    </div>
                    <div class="buttonLeftAlign">
                        <Button disabled="{!state.valid}" on:click={(e) => handleRegisterSubmit(e)}>Registrieren</Button>
                    </div>
                    <div style="text-align: center;">
                        <small>
                            <Router>
                                <nav>
                                    Du besitzt bereits ein Account? <Link to="/login">Jetzt anmelden</Link>
                                </nav>
                            </Router>
                        </small>
                    </div>
                </div>
            </div>
        {/if}
    </div>
    </div>
</div>

<style>
    .center {
        display: flex;
        justify-content: center;
        align-tems: center;
    }

    .loginbox {
        width: 50ch;
        border-radius: 5px;
        padding: 10px;
        box-shadow: rgba(60, 64, 67, 0.3) 0px 1px 2px 0px, rgba(60, 64, 67, 0.15) 0px 2px 6px 2px;
    }

    :global.btn-secondary {
        color: #fff;
        background-color: var(--mdc-theme-primary) !important;
        border-color:var(--mdc-theme-primary) !important;
    }

    :global.mdc-floating-label {
        color:var(--mdc-theme-primary) !important;
    }

    :global.btn-secondary:disabled {
        color: #fff;
        background-color: var(--mdc-theme-primary) !important;
        border-color:var(--mdc-theme-primary) !important;
    }
</style>