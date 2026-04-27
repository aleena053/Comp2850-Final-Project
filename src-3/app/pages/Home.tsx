import { Link } from "react-router";
import { Button } from "../components/ui/button";
import { Card } from "../components/ui/card";
import { User, Building2 } from "lucide-react";

export function Home() {
  return (
    <div className="min-h-screen flex items-center justify-center p-6">
      <div className="max-w-md w-full space-y-8">
        <div className="text-center space-y-2">
          <h1 className="text-white">FitTrack</h1>
          <p className="text-muted-foreground">Your personal fitness companion</p>
        </div>
        
        <div className="space-y-4">
          <h2 className="text-white text-center">Choose Account Type</h2>
          
          <Link to="/signup?type=personal">
            <Card className="bg-card border-border p-6 hover:bg-card/80 transition-colors cursor-pointer">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-full bg-primary flex items-center justify-center flex-shrink-0">
                  <User className="w-6 h-6 text-primary-foreground" />
                </div>
                <div className="flex-1">
                  <h3 className="text-white mb-1">Personal Account</h3>
                  <p className="text-muted-foreground">Track your own workouts and progress</p>
                </div>
              </div>
            </Card>
          </Link>

          <Link to="/signup?type=business">
            <Card className="bg-card border-border p-6 hover:bg-card/80 transition-colors cursor-pointer">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-full bg-primary flex items-center justify-center flex-shrink-0">
                  <Building2 className="w-6 h-6 text-primary-foreground" />
                </div>
                <div className="flex-1">
                  <h3 className="text-white mb-1">Business Account</h3>
                  <p className="text-muted-foreground">Manage clients and their workouts</p>
                </div>
              </div>
            </Card>
          </Link>
          
          <div className="pt-4 text-center">
            <p className="text-muted-foreground mb-2">Already have an account?</p>
            <Link to="/login" className="block">
              <Button 
                variant="outline" 
                className="w-full border-border text-white hover:bg-card"
              >
                Login
              </Button>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}