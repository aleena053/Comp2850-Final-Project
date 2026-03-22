import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { ArrowLeft, User, Building2 } from "lucide-react";

export function SignUp() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const accountType = searchParams.get("type") || "personal";
  
  const [formData, setFormData] = useState({
    username: "",
    password: "",
    confirmPassword: "",
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) {
      alert("Passwords don't match!");
      return;
    }
    // Save account type
    localStorage.setItem("accountType", accountType);
    // Mock signup - in real app would create account
    navigate("/dashboard");
  };

  return (
    <div className="min-h-screen p-6">
      <Link to="/" className="inline-flex items-center gap-2 text-white hover:text-primary mb-8">
        <ArrowLeft className="w-4 h-4" />
        Back
      </Link>
      
      <div className="max-w-md mx-auto space-y-8">
        <div className="text-center">
          <div className="flex items-center justify-center gap-3 mb-4">
            <div className="w-12 h-12 rounded-full bg-primary flex items-center justify-center">
              {accountType === "business" ? (
                <Building2 className="w-6 h-6 text-primary-foreground" />
              ) : (
                <User className="w-6 h-6 text-primary-foreground" />
              )}
            </div>
          </div>
          <h1 className="text-white mb-2">
            Create {accountType === "business" ? "Business" : "Personal"} Account
          </h1>
          <p className="text-muted-foreground">
            {accountType === "business" 
              ? "Set up your business account to manage clients"
              : "Sign up to start your fitness journey"
            }
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="username" className="text-white">Username</Label>
              <Input
                id="username"
                type="text"
                value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                className="bg-input-background border-border text-white"
                placeholder="Choose a username"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password" className="text-white">Password</Label>
              <Input
                id="password"
                type="password"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                className="bg-input-background border-border text-white"
                placeholder="Create a password"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="confirmPassword" className="text-white">Confirm Password</Label>
              <Input
                id="confirmPassword"
                type="password"
                value={formData.confirmPassword}
                onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                className="bg-input-background border-border text-white"
                placeholder="Confirm your password"
                required
              />
            </div>
          </div>

          <Button 
            type="submit" 
            className="w-full bg-primary hover:bg-primary/90 text-primary-foreground"
          >
            Continue
          </Button>
        </form>

        <p className="text-center text-muted-foreground">
          Already have an account?{" "}
          <Link to="/login" className="text-primary hover:underline">
            Login
          </Link>
        </p>
      </div>
    </div>
  );
}