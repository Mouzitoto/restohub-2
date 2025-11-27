export function RestoHubLogo({ className = "h-8" }: { className?: string }) {
  return (
    <svg 
      viewBox="0 0 200 50" 
      className={className}
      fill="none" 
      xmlns="http://www.w3.org/2000/svg"
    >
      {/* Icon - Fork and Knife */}
      <g>
        {/* Fork */}
        <path 
          d="M12 8 L12 18 M8 8 L8 13 C8 15 9 16 10 16 L14 16 C15 16 16 15 16 13 L16 8 M12 16 L12 24" 
          stroke="#E11D48" 
          strokeWidth="2" 
          strokeLinecap="round"
        />
        {/* Knife */}
        <path 
          d="M22 8 L22 24 M22 8 C22 8 26 8 26 12 C26 16 22 16 22 16" 
          stroke="#E11D48" 
          strokeWidth="2" 
          strokeLinecap="round"
        />
        {/* Circle background */}
        <circle cx="19" cy="16" r="16" fill="#FFF1F2" opacity="0.5" />
      </g>

      {/* Text - RestoHub */}
      <text 
        x="42" 
        y="32" 
        fontFamily="system-ui, -apple-system, sans-serif" 
        fontSize="24" 
        fontWeight="700" 
        fill="#E11D48"
      >
        Resto
      </text>
      <text 
        x="110" 
        y="32" 
        fontFamily="system-ui, -apple-system, sans-serif" 
        fontSize="24" 
        fontWeight="700" 
        fill="#1F2937"
      >
        Hub
      </text>
      
      {/* Tagline */}
      <text 
        x="42" 
        y="43" 
        fontFamily="system-ui, -apple-system, sans-serif" 
        fontSize="8" 
        fill="#9CA3AF"
        letterSpacing="1"
      >
        БРОНИРУЙ ЛЕГКО
      </text>
    </svg>
  );
}

