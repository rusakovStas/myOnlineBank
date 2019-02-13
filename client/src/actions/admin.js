import { GET_USERS, ADD_USER, DELETE_USER } from "./types";
import api from "../api/api";

export const getUsersFromRs = users => ({
	type: GET_USERS,
	users
});

export const addUser = user => ({
	type: ADD_USER,
	user
});

export const del = user => ({
	type: DELETE_USER,
	user
});

export const getUsers = () => dispatch => {
	api.admin.getAllUsers().then(users => {
		dispatch(getUsersFromRs(users));
	});
};

export const addNewUser = data => dispatch =>
	api.admin.addUser(data).then(addedUser => {
		dispatch(addUser(addedUser));
	});

export const deleteUser = user => dispatch =>
	api.admin.deleteUser(user).then(() => {
		dispatch(del(user));
	});
