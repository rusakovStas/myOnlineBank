import React from "react";
import PropTypes from "prop-types";
import { Container, Row, Col, Button } from "reactstrap";
import { toastr } from "react-redux-toastr";
import Account from "./Account";

class AccountForm extends React.Component {
	state = {
		collapse: -1
	};

	toggle = index => {
		this.setState({
			collapse: this.state.collapse === Number(index) ? -1 : Number(index)
		});
	};

	render() {
		return (
			<div>
				 <Container>
					<Row>
						<Col
							sm="12"
							md={{ size: 10, offset: 1 }}
							className="mb-2 mt-2"
						/>
						{this.props.accounts.map(acc => (
							<Col
								sm="12"
								md={{ size: 10, offset: 1 }}
								className="mb-3"
							>
								<Account
									currentUser={this.props.currentUser}
									account={acc}
									open={this.state.collapse === acc.id}
									toggle={this.toggle}
									suggestions={this.props.suggestions}
									transaction={this.props.transaction}
									decline={this.props.decline}
								/>
							</Col>
						))}
					</Row>
					<div className="fixed-bottom d-flex justify-content-center p-3">
						<Button
							size="lg"
							color="primary"
							onClick={() =>
								toastr.success("The title", "The message")
							}
						>
							Add new account
						</Button>
					</div>
				</Container>
			</div>
		);
	}
}

AccountForm.propTypes = {
	transaction: PropTypes.func.isRequired,
	decline: PropTypes.func.isRequired,
	edite: PropTypes.func.isRequired,
	accounts: PropTypes.arrayOf.isRequired,
	suggestions: PropTypes.arrayOf.isRequired,
	currentUser: PropTypes.string.isRequired
};

export default AccountForm;
