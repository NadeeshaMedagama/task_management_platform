export default function LoadingSpinner({ size = 'md' }: { size?: 'sm' | 'md' | 'lg' }) {
  const sizeClass = { sm: 'h-4 w-4', md: 'h-8 w-8', lg: 'h-12 w-12' }[size];
  return (
    <div className="flex items-center justify-center">
      <div className={`${sizeClass} animate-spin rounded-full border-4 border-blue-200 border-t-blue-600`} />
    </div>
  );
}

