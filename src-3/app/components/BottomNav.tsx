import { Link, useLocation } from "react-router";
import { Home, TrendingUp, Calendar, User, Users } from "lucide-react";
import { useState, useEffect } from "react";

export function BottomNav() {
  const location = useLocation();
  const [isBusinessAccount, setIsBusinessAccount] = useState(false);
  
  useEffect(() => {
    // Check if user is a business account
    const accountType = localStorage.getItem("accountType");
    setIsBusinessAccount(accountType === "business");
  }, []);
  
  const navItems = isBusinessAccount
    ? [
        { path: "/dashboard", icon: Home, label: "Home" },
        { path: "/clients", icon: Users, label: "Clients" },
        { path: "/calendar", icon: Calendar, label: "Calendar" },
        { path: "/profile", icon: User, label: "Profile" },
      ]
    : [
        { path: "/dashboard", icon: Home, label: "Home" },
        { path: "/progress", icon: TrendingUp, label: "Progress" },
        { path: "/calendar", icon: Calendar, label: "Calendar" },
        { path: "/profile", icon: User, label: "Profile" },
      ];

  // Don't show bottom nav on auth pages
  if (["/", "/login", "/signup"].includes(location.pathname)) {
    return null;
  }

  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-card border-t border-border max-w-[430px] mx-auto">
      <div className="px-6 py-3">
        <div className="flex justify-around items-center">
          {navItems.map((item) => {
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`flex flex-col items-center gap-1 px-4 py-2 rounded-lg transition-colors ${
                  isActive 
                    ? 'text-primary' 
                    : 'text-muted-foreground hover:text-white'
                }`}
              >
                <item.icon className="w-5 h-5" />
                <span className="text-xs">{item.label}</span>
              </Link>
            );
          })}
        </div>
      </div>
    </nav>
  );
}