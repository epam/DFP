using System;
using System.Globalization;
using System.Runtime.CompilerServices;
using System.Runtime.Serialization;
using System.Text;

[assembly: InternalsVisibleTo("EPAM.Deltix.DFP.Math")]

namespace EPAM.Deltix.DFP
{
	[Serializable()]
	public struct Decimal64 : IComparable<Decimal64>, IEquatable<Decimal64>, ISerializable
	{
		#region Constants
		public static readonly Decimal64 Null		= new Decimal64(DotNetImpl.Null);

		public static readonly Decimal64 NaN		= new Decimal64(DotNetImpl.NaN);

		public static readonly Decimal64 PositiveInfinity = new Decimal64(DotNetImpl.PositiveInfinity);
		public static readonly Decimal64 NegativeInfinity = new Decimal64(DotNetImpl.NegativeInfinity);

		public static readonly Decimal64 MinValue	= new Decimal64(DotNetImpl.MinValue);
		public static readonly Decimal64 MaxValue	= new Decimal64(DotNetImpl.MaxValue);

		public static readonly Decimal64 MinPositiveValue = new Decimal64(DotNetImpl.MinPositiveValue);
		public static readonly Decimal64 MaxNegativeValue = new Decimal64(DotNetImpl.MaxNegativeValue);

		public static readonly Decimal64 Zero		= new Decimal64(DotNetImpl.Zero);
		public static readonly Decimal64 One		= new Decimal64(DotNetImpl.One);
		public static readonly Decimal64 Two		= new Decimal64(DotNetImpl.Two);
		public static readonly Decimal64 Ten		= new Decimal64(DotNetImpl.Ten);
		public static readonly Decimal64 Hundred	= new Decimal64(DotNetImpl.Hundred);
		public static readonly Decimal64 Thousand	= new Decimal64(DotNetImpl.Thousand);
		public static readonly Decimal64 Million	= new Decimal64(DotNetImpl.Million);

		public static readonly Decimal64 OneTenth	= new Decimal64(DotNetImpl.OneTenth);
		public static readonly Decimal64 OneHundredth = new Decimal64(DotNetImpl.OneHundredth);

		public static readonly int MaxSignificandDigits = 16;
		public static readonly int MaxExponent = 384;
		public static readonly int MinExponent = -383;

		public const char DecimalMarkDot = '.';
		public const char DecimalMarkComma = ',';
		public const char DecimalMarkDefault = DecimalMarkDot;
		public static string DecimalMarkAny = "" + DecimalMarkDot + DecimalMarkComma;

		#endregion

		public UInt64 Bits { get; }

		internal Decimal64(UInt64 value)
		{
			Bits = value;
		}

		#region Standard overloads

		public override String ToString()
		{
			return DotNetImpl.ToString(Bits, DecimalMarkDefault);
		}

		public String ToString(char decimalMark)
		{
			return DotNetImpl.ToString(Bits, decimalMark);
			//return ((Double)this).ToString(CultureInfo.InvariantCulture);
		}

		public override Boolean Equals(Object obj)
		{
			return obj is Decimal64 && Equals((Decimal64)obj);
		}

		public override Int32 GetHashCode()
		{
			UInt64 bits = Canonize().Bits;
			return bits.GetHashCode();
		}

		#endregion

		#region Conversion

		public static Decimal64 FromFixedPoint(int mantissa, int numberOfDigits)
		{
			return new Decimal64(DotNetImpl.FromFixedPoint32(mantissa, numberOfDigits));
		}

		public static Decimal64 FromFixedPoint(uint mantissa, int numberOfDigits)
		{
			return new Decimal64(DotNetImpl.FromFixedPoint32(mantissa, numberOfDigits));
		}

		public static Decimal64 FromFixedPoint(long mantissa, int numberOfDigits)
		{
			// TODO: More optimizations
			return new Decimal64(
				0 == (mantissa & (-1L << 53))
				? DotNetImpl.FromFixedPointLimitedU64((UInt64)mantissa, numberOfDigits)
				: NativeImpl.fromFixedPoint64(mantissa, numberOfDigits));
		}

		public static Decimal64 FromFixedPoint(int mantissa, uint numberOfDigits)
		{
			return numberOfDigits < Int32.MaxValue ? FromFixedPoint(mantissa, (int)numberOfDigits) : Decimal64.Zero;
		}

		public static Decimal64 FromFixedPoint(uint mantissa, uint numberOfDigits)
		{
			return numberOfDigits < Int32.MaxValue ? FromFixedPoint(mantissa, (int)numberOfDigits) : Decimal64.Zero;
		}

		public static Decimal64 FromFixedPoint(long mantissa, uint numberOfDigits)
		{
			return numberOfDigits < Int32.MaxValue ? FromFixedPoint(mantissa, (int)numberOfDigits) : Decimal64.Zero;
		}

		public Int64 ToFixedPoint(int numberOfDigits)
		{
			return NativeImpl.toFixedPoint(Bits, numberOfDigits);
		}

		public static Decimal64 FromLong(long value)
		{
			return new Decimal64(NativeImpl.fromInt64(value));
		}

		public long ToLong()
		{
			return NativeImpl.toInt64(Bits);
		}

		public static Decimal64 FromULong(ulong value)
		{
			return new Decimal64(NativeImpl.fromUInt64(value));
		}

		public ulong ToULong()
		{
			return NativeImpl.toUInt64(Bits);
		}

		public static Decimal64 FromInt(int value)
		{
			return new Decimal64(DotNetImpl.FromInt32(value));
		}

		public int ToInt()
		{
			return (int)NativeImpl.toInt64(Bits);
		}

		public static Decimal64 FromUInt(uint value)
		{
			return new Decimal64(DotNetImpl.FromUInt32(value));
		}

		public uint ToUInt()
		{
			return (uint)NativeImpl.toUInt64(Bits);
		}

		public short ToShort()
		{
			return (short)NativeImpl.toInt64(Bits);
		}

		public ushort ToUShort()
		{
			return (ushort)NativeImpl.toUInt64(Bits);
		}

		public sbyte ToSByte()
		{
			return (sbyte)NativeImpl.toInt64(Bits);
		}

		public byte ToByte()
		{
			return (byte)NativeImpl.toUInt64(Bits);
		}

		public static Decimal64 FromUnderlying(UInt64 bits)
		{
			return new Decimal64(bits);
		}

		public UInt64 ToUnderlying()
		{
			return Bits;
		}

		public static Decimal64 FromDouble(Double value)
		{
			return new Decimal64(NativeImpl.fromFloat64(value));
		}

		public static Decimal64 FromDecimalDouble(Double value)
		{
			return new Decimal64(DotNetImpl.FromDecimalFloat64(value));
		}

		public Double ToDouble()
		{
			return NativeImpl.toFloat64(Bits);
		}

		public static Decimal64 FromDecimal(Decimal value)
		{
			return new Decimal64(DotNetImpl.FromDecimal(value));
		}

		public Decimal ToDecimal()
		{
			return DotNetImpl.ToDecimal(ToUnderlying());
		}

		#endregion

		#region Conversion(Explicit operators)

		public static explicit operator Decimal64(long value)
		{
			return FromLong(value);
		}

		public static explicit operator long(Decimal64 decimal64)
		{
			return decimal64.ToLong();
		}

		public static explicit operator Decimal64(ulong value)
		{
			return FromULong(value);
		}

		public static explicit operator ulong(Decimal64 decimal64)
		{
			return decimal64.ToULong();
		}

		public static explicit operator Decimal64(int value)
		{
			return FromInt(value);
		}

		public static explicit operator int(Decimal64 decimal64)
		{
			return decimal64.ToInt();
		}

		public static explicit operator Decimal64(uint value)
		{
			return FromUInt(value);
		}

		public static explicit operator uint(Decimal64 decimal64)
		{
			return decimal64.ToUInt();
		}

		public static explicit operator Decimal64(short value)
		{
			return FromInt(value);
		}

		public static explicit operator short(Decimal64 decimal64)
		{
			return decimal64.ToShort();
		}

		public static explicit operator Decimal64(ushort value)
		{
			return FromUInt(value);
		}

		public static explicit operator ushort(Decimal64 decimal64)
		{
			return decimal64.ToUShort();
		}

		public static explicit operator Decimal64(sbyte value)
		{
			return FromInt(value);
		}

		public static explicit operator sbyte(Decimal64 decimal64)
		{
			return decimal64.ToSByte();
		}

		public static explicit operator Decimal64(byte value)
		{
			return FromUInt(value);
		}

		public static explicit operator byte(Decimal64 decimal64)
		{
			return decimal64.ToByte();
		}

		public static explicit operator Decimal64(Double value)
		{
			return FromDouble(value);
		}

		public static explicit operator double(Decimal64 decimal64)
		{
			return decimal64.ToDouble();
		}

		public static explicit operator Decimal(Decimal64 decimal64)
		{
			return decimal64.ToDecimal();
		}

		public static explicit operator Decimal64(Decimal value)
		{
			return FromDecimal(value);
		}

		#endregion

		#region Classification

		/// <summary>
		/// Returns <code>true</code> if the supplied <code>DFP</code> value equals dedicated <code>NULL</code> constant
		/// (in the range of the NaN values) that imply null reference of type <see cref="Decimal64"/>.
		/// </summary>
		/// <returns><code>true</code> for dedicated <code>NULL</code> constant</returns>
		public Boolean IsNull()
		{
			return DotNetImpl.IsNull(Bits);
		}

		/// <summary>
		/// Checks is the <code>DFP</code> value is Not-a-Number.
		/// If you need check for all abnormal values use negation of the <see cref="IsFinite"/> function.
		/// </summary>
		/// <returns><code>true</code> for Not-a-Number values.</returns>
		public Boolean IsNaN()
		{
			return DotNetImpl.IsNaN(Bits);
		}

		/// <summary>
		/// Checks is the <code>DFP</code> value is positive or negative infinity.
		/// If you need check for all abnormal values use negation of the <see cref="IsFinite"/> function.
		/// </summary>
		/// <returns><code>true</code> for positive or negative infinity.</returns>
		public Boolean IsInfinity()
		{
			return DotNetImpl.IsInfinity(Bits);
		}

		/// <summary>
		/// Checks is the <code>DFP</code> value is positive infinity.
		/// If you need check for all abnormal values use negation of the <see cref="IsFinite"/> function.
		/// </summary>
		/// <returns><code>true</code> for positive infinity.</returns>
		public Boolean IsPositiveInfinity()
		{
			return DotNetImpl.IsPositiveInfinity(Bits);
		}

		/// <summary>
		/// Checks is the <code>DFP</code> value is negative infinity.
		/// If you need check for all abnormal values use negation of the <see cref="IsFinite"/> function.
		/// </summary>
		/// <returns><code>true</code> for negative infinity.</returns>
		public Boolean IsNegativeInfinity()
		{
			return DotNetImpl.IsNegativeInfinity(Bits);
		}

		[Obsolete("IsSigned is deprecated, please use IsNegative instead for actual comparison with 0")]
		public Boolean IsSigned()
		{
			return DotNetImpl.SignBit(Bits);
		}

		/// <summary>
		/// Checks is the <code>DFP</code> value is a finite value: not a NaN, not a positive infinity, not a negative infinity.
		/// </summary>
		/// <returns><code>true</code> for finite values.</returns>
		public Boolean IsFinite()
		{
			return DotNetImpl.IsFinite(Bits);
		}

		public Boolean IsNormal()
		{
			return NativeImpl.isNormal(Bits);
		}

		#endregion

		#region Comparison

		public Boolean IsEqual(Decimal64 that)
		{
			UInt64 aBits = Bits, bBits = that.Bits;
			return aBits == bBits || NativeImpl.isEqual(aBits, bBits);
		}

		public static Boolean operator ==(Decimal64 a, Decimal64 b)
		{
			return a.IsEqual(b);
		}

		public Boolean IsIdentical(Decimal64 that)
		{
			return Bits == that.Bits;
		}

		public Boolean IsNotEqual(Decimal64 that)
		{
			UInt64 aBits = Bits, bBits = that.Bits;
			return aBits != bBits && NativeImpl.isNotEqual(aBits, bBits);
		}

		public static Boolean operator !=(Decimal64 a, Decimal64 b)
		{
			return a.IsNotEqual(b);
		}

		public Boolean IsGreater(Decimal64 that)
		{
			return NativeImpl.isGreater(Bits, that.Bits);
		}

		public static Boolean operator >(Decimal64 a, Decimal64 b)
		{
			return NativeImpl.isGreater(a.Bits, b.Bits);
		}

		public Boolean IsLess(Decimal64 that)
		{
			return NativeImpl.isLess(Bits, that.Bits);
		}

		public static Boolean operator <(Decimal64 a, Decimal64 b)
		{
			return NativeImpl.isLess(a.Bits, b.Bits);
		}

		public Boolean IsGreaterOrEqual(Decimal64 that)
		{
			return NativeImpl.isGreaterOrEqual(Bits, that.Bits);
		}

		public static Boolean operator >=(Decimal64 a, Decimal64 b)
		{
			return NativeImpl.isGreaterOrEqual(a.Bits, b.Bits);
		}

		public Boolean IsLessOrEqual(Decimal64 that)
		{
			return NativeImpl.isLessOrEqual(Bits, that.Bits);
		}

		public static Boolean operator <=(Decimal64 a, Decimal64 b)
		{
			return NativeImpl.isLessOrEqual(a.Bits, b.Bits);
		}

		/// <summary>
		/// Checks is the <code>DFP</code> value is zero.
		/// </summary>
		/// <returns><code>true</code> for zero.</returns>
		public Boolean IsZero()
		{
			return DotNetImpl.IsZero(Bits);
		}

		/// <summary>
		/// Checks is the <code>DFP</code> value is not a zero or abnormal: NaN or positive infinity or negative infinity.
		/// </summary>
		/// <returns><code>true</code> for not a zero or abnormal.</returns>
		public Boolean IsNonZero()
		{
			return !DotNetImpl.IsZero(Bits);
		}

		/// <summary>
		/// Checks is the <code>DFP</code> value is greater than zero.
		/// If you need check for values greater or equal to zero use <see cref="IsNonNegative"/> function.
		/// </summary>
		/// <returns><code>true</code> for values greater than zero.</returns>
		public Boolean IsPositive()
		{
			return DotNetImpl.IsPositive(Bits);
		}

		/// <summary>
		/// Checks is the <code>DFP</code> value is less than zero.
		/// If you need check for values less or equal to zero use <see cref="IsNonPositive"/> function.
		/// </summary>
		/// <returns><code>true</code> for values less than zero.</returns>
		public Boolean IsNegative()
		{
			return DotNetImpl.IsNegative(Bits);
		}

		/// <summary>
		/// Checks is the <code>DFP</code> value is less or equal to zero.
		/// If you need check for values strictly less than zero use <see cref="IsNegative"/> function.
		/// </summary>
		/// <returns><code>true</code> for values less or equal to zero.</returns>
		public Boolean IsNonPositive()
		{
			return DotNetImpl.IsNonPositive(Bits);
		}

		/// <summary>
		/// Checks is the <code>DFP</code> value is greater or equal to zero.
		/// If you need check for values strictly greater than zero use <see cref="IsPositive"/> function.
		/// </summary>
		/// <returns><code>true</code> for values greater or equal to zero.</returns>
		public Boolean IsNonNegative()
		{
			return DotNetImpl.IsNonNegative(Bits);
		}

		#endregion

		#region Minimum & Maximum

		public static Decimal64 Max(Decimal64 a, Decimal64 b)
		{
			return new Decimal64(NativeImpl.max2(a.Bits, b.Bits));
		}

		public static Decimal64 Max(Decimal64 a, Decimal64 b, Decimal64 c)
		{
			return new Decimal64(NativeImpl.max3(a.Bits, b.Bits, c.Bits));
		}

		public static Decimal64 Max(Decimal64 a, Decimal64 b, Decimal64 c, Decimal64 d)
		{
			return new Decimal64(NativeImpl.max4(a.Bits, b.Bits, c.Bits, d.Bits));
		}

		public static Decimal64 Min(Decimal64 a, Decimal64 b)
		{
			return new Decimal64(NativeImpl.min2(a.Bits, b.Bits));
		}

		public static Decimal64 Min(Decimal64 a, Decimal64 b, Decimal64 c)
		{
			return new Decimal64(NativeImpl.min3(a.Bits, b.Bits, c.Bits));
		}

		public static Decimal64 Min(Decimal64 a, Decimal64 b, Decimal64 c, Decimal64 d)
		{
			return new Decimal64(NativeImpl.min4(a.Bits, b.Bits, c.Bits, d.Bits));
		}

		public Decimal64 Max(Decimal64 b)
		{
			return new Decimal64(NativeImpl.max2(Bits, b.Bits));
		}

		public Decimal64 Min(Decimal64 b)
		{
			return new Decimal64(NativeImpl.min2(Bits, b.Bits));
		}

		#endregion

		#region Arithmetic

		public static Decimal64 operator +(Decimal64 value)
		{
			return value;
		}

		public Decimal64 Negate()
		{
			return new Decimal64(DotNetImpl.Negate(Bits));
		}

		public static Decimal64 operator -(Decimal64 value)
		{
			return new Decimal64(DotNetImpl.Negate(value.Bits));
		}

		public Decimal64 Abs()
		{
			return new Decimal64(DotNetImpl.Abs(Bits));
		}

		public Decimal64 Add(Decimal64 b)
		{
			return new Decimal64(NativeImpl.add2(Bits, b.Bits));
		}

		public Decimal64 Add(Decimal64 b, Decimal64 c)
		{
			return new Decimal64(NativeImpl.add3(Bits, b.Bits, c.Bits));
		}

		public Decimal64 Add(Decimal64 b, Decimal64 c, Decimal64 d)
		{
			return new Decimal64(NativeImpl.add4(Bits, b.Bits, c.Bits, d.Bits));
		}

		public static Decimal64 operator +(Decimal64 a, Decimal64 b)
		{
			return new Decimal64(NativeImpl.add2(a.Bits, b.Bits));
		}

		public Decimal64 Subtract(Decimal64 b)
		{
			return new Decimal64(NativeImpl.subtract(Bits, b.Bits));
		}

		public static Decimal64 operator -(Decimal64 a, Decimal64 b)
		{
			return new Decimal64(NativeImpl.subtract(a.Bits, b.Bits));
		}

		public Decimal64 Multiply(Decimal64 b)
		{
			return new Decimal64(NativeImpl.multiply2(Bits, b.Bits));
		}

		public Decimal64 Multiply(Decimal64 b, Decimal64 c)
		{
			return new Decimal64(NativeImpl.multiply3(Bits, b.Bits, c.Bits));
		}

		public Decimal64 Multiply(Decimal64 b, Decimal64 c, Decimal64 d)
		{
			return new Decimal64(NativeImpl.multiply4(Bits, b.Bits, c.Bits, d.Bits));
		}

		public static Decimal64 operator *(Decimal64 a, Decimal64 b)
		{
			return new Decimal64(NativeImpl.multiply2(a.Bits, b.Bits));
		}

		public Decimal64 MultiplyByInteger(Int32 b)
		{
			return new Decimal64(NativeImpl.multiplyByInt32(Bits, b));
		}

		public Decimal64 MultiplyByInteger(Int64 b)
		{
			return new Decimal64(NativeImpl.multiplyByInt64(Bits, b));
		}

		public static Decimal64 operator *(Decimal64 a, Int32 b)
		{
			return new Decimal64(NativeImpl.multiplyByInt32(a.Bits, b));
		}

		public static Decimal64 operator *(Int32 a, Decimal64 b)
		{
			return new Decimal64(NativeImpl.multiplyByInt32(b.Bits, a));
		}

		public static Decimal64 operator *(Decimal64 a, Int64 b)
		{
			return new Decimal64(NativeImpl.multiplyByInt64(a.Bits, b));
		}

		public static Decimal64 operator *(Int64 a, Decimal64 b)
		{
			return new Decimal64(NativeImpl.multiplyByInt64(b.Bits, a));
		}

		public Decimal64 Divide(Decimal64 b)
		{
			return new Decimal64(NativeImpl.divide(Bits, b.Bits));
		}

		public static Decimal64 operator /(Decimal64 a, Decimal64 b)
		{
			return new Decimal64(NativeImpl.divide(a.Bits, b.Bits));
		}

		public Decimal64 DivideByInteger(Int32 b)
		{
			return new Decimal64(NativeImpl.divideByInt32(Bits, b));
		}

		public Decimal64 DivideByInteger(Int64 b)
		{
			return new Decimal64(NativeImpl.divideByInt64(Bits, b));
		}

		public static Decimal64 operator /(Decimal64 a, Int32 b)
		{
			return new Decimal64(NativeImpl.divideByInt32(a.Bits, b));
		}

		public static Decimal64 operator /(Decimal64 a, Int64 b)
		{
			return new Decimal64(NativeImpl.divideByInt64(a.Bits, b));
		}

		public Decimal64 MultiplyAndAdd(Decimal64 b, Decimal64 c)
		{
			return new Decimal64(NativeImpl.multiplyAndAdd(Bits, b.Bits, c.Bits));
		}

		public Decimal64 ScaleByPowerOfTen(Int32 n)
		{
			return new Decimal64(NativeImpl.scaleByPowerOfTen(Bits, n));
		}

		public Decimal64 Mean(Decimal64 b)
		{
			return new Decimal64(NativeImpl.mean2(Bits, b.Bits));
		}

		#endregion

		#region Rounding

		public Decimal64 Ceiling()
		{
			return new Decimal64(NativeImpl.roundTowardsPositiveInfinity(Bits));
		}

		public Decimal64 RoundTowardsPositiveInfinity()
		{
			return new Decimal64(NativeImpl.roundTowardsPositiveInfinity(Bits));
		}

		public Decimal64 Floor()
		{
			return new Decimal64(NativeImpl.roundTowardsNegativeInfinity(Bits));
		}

		public Decimal64 RoundTowardsNegativeInfinity()
		{
			return new Decimal64(NativeImpl.roundTowardsNegativeInfinity(Bits));
		}

		public Decimal64 RoundTowardsZero()
		{
			return new Decimal64(NativeImpl.roundTowardsZero(Bits));
		}

		/// Identical to RoundToNearestTiesAwayFromZero
		public Decimal64 Round()
		{
			return new Decimal64(NativeImpl.roundToNearestTiesAwayFromZero(Bits));
		}

		public Decimal64 RoundToNearestTiesAwayFromZero()
		{
			return new Decimal64(NativeImpl.roundToNearestTiesAwayFromZero(Bits));
		}

		public Decimal64 RoundToNearestTiesToEven()
		{
			return new Decimal64(NativeImpl.roundToNearestTiesToEven(Bits));
		}

		public Decimal64 RoundTowardsPositiveInfinity(Decimal64 multiple)
		{
			if (!multiple.IsFinite() || multiple.IsNonPositive())
				throw new ArgumentException("Multiple must be a positive finite number.");
			if (IsNaN())
				return this;

			UInt64 ratio = NativeImpl.roundTowardsPositiveInfinity(NativeImpl.divide(Bits, multiple.Bits));
			return new Decimal64(NativeImpl.multiply2(ratio, multiple.Bits));
		}

		public Decimal64 RoundTowardsNegativeInfinity(Decimal64 multiple)
		{
			if (!multiple.IsFinite() || multiple.IsNonPositive())
				throw new ArgumentException("Multiple must be a positive finite number.");
			if (IsNaN())
				return this;

			UInt64 ratio = NativeImpl.roundTowardsNegativeInfinity(NativeImpl.divide(Bits, multiple.Bits));
			return new Decimal64(NativeImpl.multiply2(ratio, multiple.Bits));
		}

		public Decimal64 RoundToNearestTiesAwayFromZero(Decimal64 multiple)
		{
			if (!multiple.IsFinite() || multiple.IsNonPositive())
				throw new ArgumentException("Multiple must be a positive finite number.");
			if (IsNaN())
				return this;

			UInt64 ratio = NativeImpl.roundToNearestTiesAwayFromZero(NativeImpl.divide(Bits, multiple.Bits));
			return new Decimal64(NativeImpl.multiply2(ratio, multiple.Bits));
		}

		public Decimal64 RoundToNearestTiesToEven(Decimal64 multiple)
		{
			if (!multiple.IsFinite() || multiple.IsNonPositive())
				throw new ArgumentException("Multiple must be a positive finite number.");
			if (IsNaN())
				return this;

			UInt64 ratio = NativeImpl.roundToNearestTiesToEven(NativeImpl.divide(Bits, multiple.Bits));
			return new Decimal64(NativeImpl.multiply2(ratio, multiple.Bits));
		}

		public Decimal64 Round(int n, RoundingMode roundType)
		{
			return new Decimal64(DotNetImpl.Round(Bits, n, roundType));
		}

		public bool IsRounded(int n)
		{
			return DotNetImpl.IsRounded(Bits, n);
		}

		public Decimal64 RoundToReciprocal(uint r, RoundingMode roundType)
		{
			return new Decimal64(DotNetImpl.RoundToReciprocal(Bits, r, roundType));
		}

		public bool IsRoundedToReciprocal(uint r)
		{
			return DotNetImpl.IsRoundedToReciprocal(Bits, r);
		}

		#endregion

		#region Parts processing

		/// <summary>
		/// Returns the unscaled value of the <see cref="Decimal64"/> in the same way as Java's BigDecimal#unscaledValue() do.
		/// For abnormal values return <see cref="long.MinValue"/>.
		/// </summary>
		/// <returns>The unscaled value of the <see cref="Decimal64"/>.</returns>
		public long GetUnscaledValue() => GetUnscaledValue(long.MinValue);

		/// <summary>
		/// Returns the unscaled value of the <see cref="Decimal64"/> in the same way as Java's BigDecimal#unscaledValue() do.
		/// </summary>
		/// <param name="abnormalReturn">The value returned for abnormal values (NaN, +Inf, -Inf).</param>
		/// <returns>The unscaled value of the <see cref="Decimal64"/>.</returns>
		public long GetUnscaledValue(long abnormalReturn)
		{
			var value = Bits;
			bool sign = DotNetImpl.SignBit(value);

			if ((value & DotNetImpl.SpecialEncodingMask) != DotNetImpl.SpecialEncodingMask)
			{
				long coefficient = (long)(value & DotNetReImpl.SMALL_COEFF_MASK64);
				return sign ? -coefficient : coefficient;
			}
			else
			{
				// special encodings
				if ((value & DotNetReImpl.INFINITY_MASK64) == DotNetReImpl.INFINITY_MASK64)
				{
					return abnormalReturn;    // NaN or Infinity
				}
				else
				{
					ulong coeff = (value & DotNetReImpl.LARGE_COEFF_MASK64) | DotNetReImpl.LARGE_COEFF_HIGH_BIT64;
					if (coeff >= 10000000000000000UL)
						coeff = 0;
					return sign ? -(long)coeff : (long)coeff;
				}
			}

		}

		/// <summary>
		/// Returns the scale of the <see cref="Decimal64"/> value in the same way as Java's BigDecimal#scale() do.
		/// For abnormal values return <see cref="int.MinValue"/>.
		/// </summary>
		/// <returns>The scale of the <see cref="Decimal64"/>.</returns>
		public int GetScale() => GetScale(int.MinValue);

		/// <summary>
		/// Returns the scale of the <see cref="Decimal64"/> value in the same way as Java's BigDecimal#scale() do.
		/// </summary>
		/// <param name="abnormalReturn">The value returned for abnormal values (NaN, +Inf, -Inf).</param>
		/// <returns>The scale of the <see cref="Decimal64"/>.</returns>
		public int GetScale(int abnormalReturn)
		{
			var value = Bits;

			if ((value & DotNetImpl.SpecialEncodingMask) != DotNetImpl.SpecialEncodingMask)
			{
				return -((int)((value >> DotNetReImpl.EXPONENT_SHIFT_SMALL64) & DotNetReImpl.EXPONENT_MASK64) - DotNetReImpl.DECIMAL_EXPONENT_BIAS);
			}
			else
			{
				// special encodings
				if ((value & DotNetReImpl.INFINITY_MASK64) == DotNetReImpl.INFINITY_MASK64)
					return abnormalReturn;
				else
					return -((int)((value >> DotNetReImpl.EXPONENT_SHIFT_LARGE64) & DotNetReImpl.EXPONENT_MASK64) - DotNetReImpl.DECIMAL_EXPONENT_BIAS);
			}
		}

		/// endregion

		#endregion

		#region Special

		public Decimal64 NextUp()
		{
			return new Decimal64(NativeImpl.nextUp(Bits));
		}

		public Decimal64 NextDown()
		{
			return new Decimal64(NativeImpl.nextDown(Bits));
		}

		/// <summary>
		/// Returns canonical representation of Decimal.
		/// We consider that all binary representations of one arithmetic value have the same canonical binary representation.
		/// Canonical representation of zeros = <see cref="Zero"/>>
		/// Canonical representation of NaNs = <see cref="NaN"/>
		/// Canonical representation of PositiveInfinities = <see cref="PositiveInfinity"/>
		/// Canonical representation of NegativeInfinities = <see cref="NegativeInfinity"/>
		/// </summary>
		/// <returns>Canonical representation of decimal argument.</returns>
		public Decimal64 Canonize()
		{
			if (IsNaN())
			{
				return NaN;
			}
			if (IsInfinity())
			{
				if (IsPositiveInfinity())
				{
					return PositiveInfinity;
				}
				else
				{
					return NegativeInfinity;
				}
			}
			return new Decimal64(DotNetImpl.Canonize(this.Bits));
		}


		#endregion

		#region Formatting & Parsing

		public static Decimal64 Parse(String text)
		{
			uint fpsf;
			var ret = DotNetReImpl.bid64_from_string(text, DecimalMarkDot, out fpsf);
			if ((fpsf & DotNetReImpl.BID_INVALID_FORMAT) != 0)
				throw new FormatException("Input string is not in a correct format.");
			//else if ((fpsf & DotNetReImpl.BID_INEXACT_EXCEPTION) != 0)
			//	throw new FormatException("Can't convert input string to value without precision loss.");
			return FromUnderlying(ret);
		}

		public static Decimal64 Parse(String text, String decimalMarks)
		{
			uint fpsf;
			var ret = DotNetReImpl.bid64_from_string(text, decimalMarks, out fpsf);
			if ((fpsf & DotNetReImpl.BID_INVALID_FORMAT) != 0)
				throw new FormatException("Input string is not in a correct format.");
			//else if ((fpsf & DotNetReImpl.BID_INEXACT_EXCEPTION) != 0)
			//	throw new FormatException("Can't convert input string to value without precision loss.");
			return FromUnderlying(ret);
		}

		public enum StatusValue
		{
			Exact = (int)DotNetReImpl.BID_EXACT_STATUS,

			Overflow = (int)DotNetReImpl.BID_OVERFLOW_EXCEPTION,

			Underflow = (int)DotNetReImpl.BID_UNDERFLOW_EXCEPTION,

			Inexact = (int)DotNetReImpl.BID_INEXACT_EXCEPTION,

			InvalidFormat = (int)DotNetReImpl.BID_INVALID_FORMAT
		}

		public static Boolean TryParse(String text, out Decimal64 result, out StatusValue status)
		{
			uint fpsf;
			result = FromUnderlying(DotNetReImpl.bid64_from_string(text, DecimalMarkDot, out fpsf));
			status = (StatusValue)fpsf;
			return status == StatusValue.Exact;
		}

		public static Boolean TryParse(String text, String decimalMarks, out Decimal64 result, out StatusValue status)
		{
			uint fpsf;
			result = FromUnderlying(DotNetReImpl.bid64_from_string(text, decimalMarks, out fpsf));
			status = (StatusValue)fpsf;
			return status == StatusValue.Exact;
		}

		public static Boolean TryParse(String text, out Decimal64 result)
		{
			uint fpsf;
			var ret = DotNetReImpl.bid64_from_string(text, DecimalMarkDot, out fpsf);
			if ((fpsf & DotNetReImpl.BID_INVALID_FORMAT) != 0)
			{
				result = NaN;
				return false;
			}
			result = FromUnderlying(ret);
			return true;
		}

		public static Boolean TryParse(String text, String decimalMarks, out Decimal64 result)
		{
			uint fpsf;
			var ret = DotNetReImpl.bid64_from_string(text, decimalMarks, out fpsf);
			if ((fpsf & DotNetReImpl.BID_INVALID_FORMAT) != 0)
			{
				result = NaN;
				return false;
			}
			result = FromUnderlying(ret);
			return true;
		}

		public String ToScientificString()
		{
			return DotNetImpl.ToScientificString(Bits, DecimalMarkDefault);
		}

		public String ToScientificString(char decimalMark)
		{
			return DotNetImpl.ToScientificString(Bits, decimalMark);
		}

		public StringBuilder AppendTo(StringBuilder text)
		{
			return DotNetImpl.AppendTo(Bits, DecimalMarkDefault, text);
		}

		public StringBuilder AppendTo(char decimalMark, StringBuilder text)
		{
			return DotNetImpl.AppendTo(Bits, decimalMark, text);
		}

		public StringBuilder ScientificAppendTo(StringBuilder text)
		{
			return DotNetImpl.ScientificAppendTo(Bits, DecimalMarkDefault, text);
		}

		public StringBuilder ScientificAppendTo(char decimalMark, StringBuilder text)
		{
			return DotNetImpl.ScientificAppendTo(Bits, decimalMark, text);
		}

		#endregion

		#region IComparable<> Interface implementation
		public Int32 CompareTo(Decimal64 other)
		{
			return NativeImpl.compare(Bits, other.Bits);
		}

		#endregion

		#region IEquatable<> Interface implementation
		public Boolean Equals(Decimal64 other)
		{
			return Canonize().Bits == other.Canonize().Bits;
		}

		#endregion

		#region ISerializable Interface implementation

		public Decimal64(SerializationInfo info, StreamingContext context)
		{
			Bits = info.GetUInt64("");
		}

		public void GetObjectData(SerializationInfo info, StreamingContext context)
		{
			info.AddValue("", Bits);
		}

		#endregion
	}
}
