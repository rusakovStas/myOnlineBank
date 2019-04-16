import { toastr } from "react-redux-toastr";
import {
	GET_ACCOUNTS,
	ADD_ACCOUNT,
	UPDATE_ACCOUNT,
	DELETE_ACCOUNT
} from "./types";
import api from "../api/api";
import webSocket from "../api/web-socket";

export const getAccountsFromRs = accounts => ({
	type: GET_ACCOUNTS,
	accounts
});

export const addAccount = account => ({
	type: ADD_ACCOUNT,
	account
});

export const updateAccount = account => ({
	type: UPDATE_ACCOUNT,
	account
});

export const del = id => ({
	type: DELETE_ACCOUNT,
	id
});

export const getAllAccounts = username => dispatch => {
	api.admin.getAllAccounts().then(accounts => {
		dispatch(getAccountsFromRs(accounts));
	});
	webSocket.stompClient.onEvent(`/topic/accounts/${username}`, response => {
		dispatch(updateAccount(response));
	});
	webSocket.stompClient.onEvent(`/topic/push/${username}`, response => {
		toastr.success("Message from server", response.msg);
	});
};

export const getMyAccounts = username => dispatch => {
	api.account.getMyAccounts().then(accounts => {
		dispatch(getAccountsFromRs(accounts));
	});
	webSocket.stompClient.onEvent(`/topic/accounts/${username}`, response => {
		dispatch(updateAccount(response));
	});
	webSocket.stompClient.onEvent(`/topic/push/${username}`, response => {
		toastr.success("Message from server", response.msg);
	});
};

export const deleteAccount = accountId => dispatch =>
	api.account.deleteAccount(accountId).then(() => {
		dispatch(del(accountId));
	});

export const createAccount = account => dispatch =>
	api.account.createAccount(account).then(response => {
		dispatch(addAccount(response));
	});
