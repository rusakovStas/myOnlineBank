import React from "react";
import PropTypes from "prop-types";
import { Container, Row, Col, Button } from "reactstrap";
import Account from "./Account";

class AccountForm extends React.Component {
	state = {
		collapse: -1,
		suggestions: []
	};

	componentDidMount() {
		this.props
			.getSuggestions()
			.then(res => this.setState({ suggestions: res }));
	}

	toggle = index => {
		this.setState({
			collapse: this.state.collapse === Number(index) ? -1 : Number(index)
		});
	};

	createNewAccount = () => {
		const account = {
			user: {
				username: this.props.currentUser
			}
		};
		this.props.create(account);
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
									getSuggestions={this.props.getSuggestions}
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
							onClick={this.createNewAccount}
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
	create: PropTypes.func.isRequired,
	accounts: PropTypes.arrayOf.isRequired,
	getSuggestions: PropTypes.func.isRequired,
	currentUser: PropTypes.string.isRequired
};

export default AccountForm;
