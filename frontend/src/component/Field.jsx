export default function Field({
  label,
  error,
  as = "input",
  className = "",
  ...props
}) {
  const Component = as;

  return (
    <label className="field">
      <span className="label">{label}</span>
      <Component className={`input ${className}`.trim()} {...props} />
      {error ? <span className="mt-2 block text-sm font-medium text-rose-600">{error}</span> : null}
    </label>
  );
}
