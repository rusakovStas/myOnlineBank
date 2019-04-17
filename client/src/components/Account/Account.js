import React from "react";
import PropTypes from "prop-types";
import {
	Collapse,
	CardText,
	Card,
	Button,
	Row,
	Col,
	CardTitle,
	Input
} from "reactstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Typeahead } from "react-bootstrap-typeahead";
import FormButton from "../commons/FormButton";

class Account extends React.Component {
	state = {
		openTransaction: false,
		confirm: false,
		transaction: false,
		edite: false,
		block: false,
		loading: false,
		chosen: [],
		transactionData: {},
		suggestions: [],
		accountData: {}
	};

	constructor(props) {
		super(props);
		// create a ref to store the button DOM element
		this.inputForName = React.createRef();
		this.props
			.getSuggestions()
			.then(res => this.setState({ suggestions: res }));
	}

	componentDidUpdate(prevProps, prevState) {
		if (!prevState.edite && this.state.edite) {
			this.focus();
		}
	}

	handleFocus = event => {
		event.target.select();
	};

	focus = () => {
		this.inputForName.current.focus();
	};

	toggleTransaction = () => {
		// По хорошему нужно делать какой нибудь хитрый loading, но пока вроде нет проблем оставлю так
		this.props
			.getSuggestions()
			.then(res => this.setState({ suggestions: res }));
		this.setState({
			transaction: true,
			openTransaction: !this.state.openTransaction && this.props.open,
			confirm: true
		});
	};

	toggleEdit = () => {
		this.setState({
			edite: true,
			confirm: true
		});
	};

	toggleBlock = () => {
		this.setState({
			block: true,
			confirm: true
		});
	};

	decline = () => {
		if (this.state.block) {
			this.setState({
				confirm: false,
				block: false
			});
		}
		if (this.state.edite) {
			this.setState({
				accountData: {}
			});
			this.setState({
				confirm: false,
				edite: false
			});
		}
		if (this.state.transaction) {
			this.setState({
				confirm: false,
				transaction: false,
				openTransaction: false
			});
		}
	};

	accept = () => {
		if (this.state.block) {
			this.setState({ loading: true });
			this.props
				.decline(this.props.account.id)
				.then(() => this.setState({ loading: false }));
			this.setState({
				confirm: false,
				block: false
			});
		}
		if (this.state.edite) {
			this.setState({ loading: true });
			this.props
				.edite(this.state.accountData)
				.then(() => this.setState({ loading: false, accountData: {} }))
				.then(() =>
					this.setState({
						confirm: false,
						edite: false
					})
				);
		}
		if (this.state.transaction) {
			this.setState({ loading: true });
			this.props
				.transaction(this.state.transactionData)
				.then(() => this.setState({ loading: false }))
				.then(() =>
					this.setState({
						confirm: false,
						transaction: false,
						openTransaction: false
					})
				);
		}
	};

	handleOptionSelected = option => {
		if (option.length >= 1) {
			this.setState({
				transactionData: {
					...this.state.transactionData,
					userTo:
						option[0].userName === "My own account"
							? this.props.account.user.username
							: option[0].userName,
					accountNumberTo: option[0].maskAccountNumber,
					accountIdTo: option[0].accountId,
					userFrom: this.props.account.user.username,
					accountNumberFrom: this.props.account.number,
					accountIdFrom: this.props.account.id
				},
				chosen: option
			});
		}
	};

	onChangeAmount = e =>
		this.setState({
			transactionData: {
				...this.state.transactionData,
				amount: {
					sum: e.target.value,
					currency: this.props.account.amount.currency
				}
			}
		});

	onChangeAccountName = e =>
		this.setState({
			accountData: {
				name: e.target.value ? e.target.value : "",
				id: this.props.account.id
			}
		});

	render() {
		const { account, open, toggle, currentUser } = this.props;
		const {
			openTransaction,
			confirm,
			edite,
			block,
			loading,
			chosen,
			suggestions,
			transaction,
			accountData
		} = this.state;
		return (
			<div>
				<Card className="text-center account-item p-2 text-white grow-on-hover shadow">
					{(!!account.name || edite) && (
						<CardTitle>
							<input
								value={
									this.state.edite === true
										? accountData.name
										: account.name
								}
								id="accountName"
								disabled={this.state.edite === false}
								ref={this.inputForName}
								onFocus={this.handleFocus}
								onChange={this.onChangeAccountName}
								className="border-0 form-input form-control input-in-header text-white"
							/>
						</CardTitle>
					)}
					<CardText onClick={() => toggle(account.id)}>
						<large>
							<p>
								<b
									className="text-monospace"
									id="money-in-the-account"
								>
									{account.amount.sum}
									<FontAwesomeIcon
										icon="ruble-sign"
										size="1x"
									/>
								</b>
							</p>
						</large>

						<p className="text-monospace card-text text-right pl-1 mb-0">
							<small>
								<b id="account-number">{account.number}</b>
							</small>
						</p>

						<p className="card-text text-right pl-1 mt-0">
							<small className="text-muted" id="card-owner">
								Owner: {account.user.username}
							</small>
						</p>
					</CardText>

					<Collapse isOpen={open || confirm}>
						<Collapse isOpen={openTransaction}>
							<FontAwesomeIcon icon="arrow-down" size="2x" />
							<CardText className="pl-0 pr-0">
								<Typeahead
									renderMenuItemChildren={option => (
										<div>
											{option.userName}
											<div>
												<small>
													Account:
													{option.maskAccountNumber}
												</small>
											</div>
										</div>
									)}
									labelKey={option =>
										`${option.maskAccountNumber} ${
											option.userName
										}`
									}
									options={suggestions}
									minLength={3}
									placeholder="Type user name..."
									className="pb-2 transaction-input-test"
									onChange={this.handleOptionSelected}
									selected={chosen}
								/>
								<Input
									placeholder="Type amount..."
									type="number"
									validate
									error="wrong"
									success="right"
									id="amount"
									name="amount"
									value={transaction.amount}
									onChange={this.onChangeAmount}
									disabled={loading}
								/>
							</CardText>
						</Collapse>
						<Collapse isOpen={block}>
							<CardText className="text-danger">
								<b>
									Are you sure you want to block the account?
								</b>
							</CardText>
						</Collapse>
						<div className="m-1">
							{!confirm ? (
								<Row>
									<Col xs="6" sm="4">
										<Button
											size="lg"
											block
											color="danger"
											className="mb-2"
											disabled={
												account.user.username !==
												currentUser
											}
											onClick={this.toggleBlock}
										>
											Block
										</Button>
									</Col>
									<Col xs="6" sm="4">
										<Button
											id="updateButton"
											size="lg"
											block
											color="success"
											disabled={
												account.user.username !==
												currentUser
											}
											onClick={this.toggleEdit}
										>
											{!!account.name ||
											account.name === ""
												? "Edit"
												: "Name"}
										</Button>
									</Col>
									<Col sm="4">
										<Button
											size="lg"
											block
											color="purple"
											onClick={this.toggleTransaction}
										>
											Transaction
										</Button>
									</Col>
								</Row>
							) : (
								<Row>
									<Col xs="6">
										<FormButton
											loading={loading}
											variant="success"
											block
											size="lg"
											submit={this.accept}
										>
											Accept
										</FormButton>
									</Col>
									<Col xs="6">
										<Button
											size="lg"
											block
											color="danger"
											onClick={this.decline}
										>
											Decline
										</Button>
									</Col>
								</Row>
							)}
						</div>
					</Collapse>
				</Card>
			</div>
		);
	}
}

Account.propTypes = {
	currentUser: PropTypes.string.isRequired,
	transaction: PropTypes.func.isRequired,
	decline: PropTypes.func.isRequired,
	edite: PropTypes.func.isRequired,
	account: PropTypes.shape({
		id: PropTypes.number.isRequired,
		name: PropTypes.string,
		number: PropTypes.string.isRequired,
		user: PropTypes.shape({
			username: PropTypes.string.isRequired
		}).isRequired,
		amount: PropTypes.shape({
			sum: PropTypes.string.isRequired,
			currency: PropTypes.string.isRequired
		}).isRequired
	}).isRequired,
	getSuggestions: PropTypes.func.isRequired,
	open: PropTypes.bool.isRequired,
	toggle: PropTypes.func.isRequired
};

export default Account;
