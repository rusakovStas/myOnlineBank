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
import NumberFormat from "react-number-format";
import FormButton from "../commons/FormButton";
import InlineError from "../commons/InlineError";

class AccountForMan extends React.Component {
	state = {
		openTransaction: false,
		confirm: false,
		transaction: false,
		edite: false,
		block: false,
		loading: false,
		chosen: [],
		transactionData: {
			amount: { sum: null },
			userTo: null,
			accountIdTo: null
		},
		suggestions: [],
		accountData: {},
		errors: {}
	};

	constructor(props) {
		super(props);
		// create a ref to store the button DOM element
		this.inputForName = React.createRef();
	}

	componentDidUpdate(prevProps, prevState) {
		if (!prevState.edite && this.state.edite) {
			this.focus();
		}
	}

	validate = data => {
		const errors = {};
		if (
			!this.props.hasRoleAdmin &&
			Number(data.amount.sum) > Number(this.props.account.amount.sum)
		)
			errors.amount = "Not enough money in this account";

		if (data.userTo === null || data.accountIdTo === null) {
			errors.destination = "You must choose destination of transaction";
		}

		return errors;
	};

	handleFocus = event => {
		event.target.select();
	};

	focus = () => {
		this.inputForName.current.focus();
	};

	toggleTransaction = () => {
		this.setState({
			transaction: this.props.enableTransaction,
			openTransaction:
				this.props.enableTransaction &&
				!this.state.openTransaction &&
				this.props.open,
			confirm: this.props.enableTransaction
		});
	};

	toggleEdit = () => {
		this.setState({
			edite: this.props.enableEdit,
			confirm: this.props.enableEdit
		});
	};

	toggleBlock = () => {
		this.setState({
			block: this.props.enableBlock,
			confirm: this.props.enableBlock
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

	onChangeAccountName = e =>
		this.setState({
			accountData: {
				name: e.target.value ? e.target.value : ""
			}
		});

	accept = () => {
		if (this.state.block) {
		}
		if (this.state.edite) {
			this.props.edit(this.state.accountData.name);
			this.setState({
				loading: false,
				accountData: {},
				confirm: false,
				edite: false
			});
		}
		if (this.state.transaction) {
		}
	};

	render() {
		const { account, open, currentUser, hasRoleAdmin, toggle } = this.props;
		const {
			openTransaction,
			confirm,
			edite,
			block,
			loading,
			chosen,
			suggestions,
			transactionData,
			accountData,
			errors
		} = this.state;
		return (
			<div>
				<Card className="text-center account-item p-2 text-white shadow-lg">
					{errors.global && <InlineError text={errors.global} />}
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
					<CardText onClick={() => toggle()}>
						<large>
							<p>
								<b
									className="text-monospace"
									id="money-in-the-account"
								>
									{hasRoleAdmin &&
									currentUser === account.user.username ? (
										<FontAwesomeIcon
											icon="infinity"
											size="2x"
										/>
									) : (
										<NumberFormat
											value={account.amount.sum}
											displayType="text"
											thousandSeparator=" "
										/>
									)}
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
								{errors.destination && (
									<InlineError text={errors.destination} />
								)}
								<NumberFormat
									customInput={Input}
									decimalScale={2}
									allowNegative={false}
									thousandSeparator=" "
									placeholder="Type amount..."
									validate
									error="wrong"
									success="right"
									id="amount"
									name="amount"
									selectAllOnFocus
									value={transactionData.amount.sum}
									onValueChange={this.onChangeAmount}
									disabled={loading}
									invalid={!!errors.amount}
								/>
								{errors.amount && (
									<InlineError text={errors.amount} />
								)}
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
											disabled={!this.props.enableBlock}
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
											disabled={!this.props.enableEdit}
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
											disabled={
												!this.props.enableTransaction
											}
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

AccountForMan.propTypes = {
	hasRoleAdmin: PropTypes.bool.isRequired,
	currentUser: PropTypes.string.isRequired,
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
	toggle: PropTypes.func.isRequired,
	open: PropTypes.bool.isRequired,
	enableTransaction: PropTypes.bool.isRequired,
	enableEdit: PropTypes.bool.isRequired,
	edit: PropTypes.func.isRequired,
	enableBlock: PropTypes.bool.isRequired
};

export default AccountForMan;
