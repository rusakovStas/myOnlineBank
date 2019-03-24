import { combineReducers } from "redux";
import { reducer as toastrReducer } from "react-redux-toastr";
import user from "./user";
import users from "./users";
import accounts from "./accounts";

export default combineReducers({
	user,
	users,
	accounts,
	toastr: toastrReducer
});
