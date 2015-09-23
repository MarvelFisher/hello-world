// ------------------------------------------------------------------------------
// <auto-generated>
//    Generated by avrogen.exe, version 0.9.0.0
//    Changes to this file may cause incorrect behavior and will be lost if code
//    is regenerated
// </auto-generated>
// ------------------------------------------------------------------------------
namespace com.cyanspring.avro.generate.trade.bean
{
	using System;
	using System.Collections.Generic;
	using System.Text;
	using Avro;
	using Avro.Specific;
	
	public partial class NewOrderRequest : ISpecificRecord
	{
		public static Schema _SCHEMA = Avro.Schema.Parse(@"{""type"":""record"",""name"":""NewOrderRequest"",""namespace"":""com.cyanspring.avro.generate.trade.bean"",""fields"":[{""name"":""objectType"",""type"":""int""},{""name"":""orderId"",""type"":""string""},{""name"":""exchangeAccount"",""type"":""string""},{""name"":""symbol"",""type"":""string""},{""name"":""orderSide"",""type"":""int""},{""name"":""orderType"",""type"":""int""},{""name"":""timeInForce"",""type"":""int""},{""name"":""price"",""type"":""double""},{""name"":""quantity"",""type"":""double""},{""name"":""created"",""type"":""string""},{""name"":""clOrderId"",""type"":""string""},{""name"":""txId"",""type"":""string""}]}");
		private int _objectType;
		private string _orderId;
		private string _exchangeAccount;
		private string _symbol;
		private int _orderSide;
		private int _orderType;
		private int _timeInForce;
		private double _price;
		private double _quantity;
		private string _created;
		private string _clOrderId;
		private string _txId;
		public virtual Schema Schema
		{
			get
			{
				return NewOrderRequest._SCHEMA;
			}
		}
		public int objectType
		{
			get
			{
				return this._objectType;
			}
			set
			{
				this._objectType = value;
			}
		}
		public string orderId
		{
			get
			{
				return this._orderId;
			}
			set
			{
				this._orderId = value;
			}
		}
		public string exchangeAccount
		{
			get
			{
				return this._exchangeAccount;
			}
			set
			{
				this._exchangeAccount = value;
			}
		}
		public string symbol
		{
			get
			{
				return this._symbol;
			}
			set
			{
				this._symbol = value;
			}
		}
		public int orderSide
		{
			get
			{
				return this._orderSide;
			}
			set
			{
				this._orderSide = value;
			}
		}
		public int orderType
		{
			get
			{
				return this._orderType;
			}
			set
			{
				this._orderType = value;
			}
		}
		public int timeInForce
		{
			get
			{
				return this._timeInForce;
			}
			set
			{
				this._timeInForce = value;
			}
		}
		public double price
		{
			get
			{
				return this._price;
			}
			set
			{
				this._price = value;
			}
		}
		public double quantity
		{
			get
			{
				return this._quantity;
			}
			set
			{
				this._quantity = value;
			}
		}
		public string created
		{
			get
			{
				return this._created;
			}
			set
			{
				this._created = value;
			}
		}
		public string clOrderId
		{
			get
			{
				return this._clOrderId;
			}
			set
			{
				this._clOrderId = value;
			}
		}
		public string txId
		{
			get
			{
				return this._txId;
			}
			set
			{
				this._txId = value;
			}
		}
		public virtual object Get(int fieldPos)
		{
			switch (fieldPos)
			{
			case 0: return this.objectType;
			case 1: return this.orderId;
			case 2: return this.exchangeAccount;
			case 3: return this.symbol;
			case 4: return this.orderSide;
			case 5: return this.orderType;
			case 6: return this.timeInForce;
			case 7: return this.price;
			case 8: return this.quantity;
			case 9: return this.created;
			case 10: return this.clOrderId;
			case 11: return this.txId;
			default: throw new AvroRuntimeException("Bad index " + fieldPos + " in Get()");
			};
		}
		public virtual void Put(int fieldPos, object fieldValue)
		{
			switch (fieldPos)
			{
			case 0: this.objectType = (System.Int32)fieldValue; break;
			case 1: this.orderId = (System.String)fieldValue; break;
			case 2: this.exchangeAccount = (System.String)fieldValue; break;
			case 3: this.symbol = (System.String)fieldValue; break;
			case 4: this.orderSide = (System.Int32)fieldValue; break;
			case 5: this.orderType = (System.Int32)fieldValue; break;
			case 6: this.timeInForce = (System.Int32)fieldValue; break;
			case 7: this.price = (System.Double)fieldValue; break;
			case 8: this.quantity = (System.Double)fieldValue; break;
			case 9: this.created = (System.String)fieldValue; break;
			case 10: this.clOrderId = (System.String)fieldValue; break;
			case 11: this.txId = (System.String)fieldValue; break;
			default: throw new AvroRuntimeException("Bad index " + fieldPos + " in Put()");
			};
		}
	}
}