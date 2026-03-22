import { Link } from "react-router";
import { ArrowLeft, User } from "lucide-react";
import { Card } from "../components/ui/card";
import { Button } from "../components/ui/button";
import { BottomNav } from "../components/BottomNav";

const circuits = [
  { id: 1, name: "Circuit 1", exercises: 8, duration: "30 min" },
  { id: 2, name: "Circuit 2", exercises: 6, duration: "25 min" },
  { id: 3, name: "Circuit 3", exercises: 10, duration: "40 min" },
  { id: 4, name: "Circuit 4", exercises: 5, duration: "20 min" },
];

export function Profile() {
  return (
    <>
      <div className="min-h-screen p-6 pb-24">
        <div className="max-w-4xl mx-auto space-y-6">
          <div className="flex items-center gap-4">
            <Link to="/dashboard">
              <button className="text-white hover:text-primary">
                <ArrowLeft className="w-6 h-6" />
              </button>
            </Link>
            <h1 className="text-white">User Profile</h1>
          </div>

          <div className="flex items-center gap-4">
            <div className="w-20 h-20 rounded-full bg-primary flex items-center justify-center">
              <User className="w-10 h-10 text-primary-foreground" />
            </div>
            <div>
              <h2 className="text-white">Athlete Name</h2>
              <p className="text-muted-foreground">Member since March 2026</p>
            </div>
          </div>

          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-white">Training Circuits</h3>
              <Button 
                size="sm" 
                className="bg-primary hover:bg-primary/90 text-primary-foreground"
              >
                Create New
              </Button>
            </div>

            <div className="space-y-3">
              {circuits.map((circuit) => (
                <Card key={circuit.id} className="bg-card border-border p-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="text-white mb-1">{circuit.name}</h4>
                      <div className="flex gap-4">
                        <span className="text-muted-foreground">
                          {circuit.exercises} exercises
                        </span>
                        <span className="text-muted-foreground">
                          {circuit.duration}
                        </span>
                      </div>
                    </div>
                    <Button 
                      size="sm" 
                      variant="outline"
                      className="border-border text-white hover:bg-card/50"
                    >
                      View
                    </Button>
                  </div>
                </Card>
              ))}
            </div>
          </div>

          <div className="flex gap-4">
            <Link to="/progress" className="flex-1">
              <Button 
                className="w-full bg-primary hover:bg-primary/90 text-primary-foreground"
              >
                View Progress
              </Button>
            </Link>
            <Link to="/calendar" className="flex-1">
              <Button 
                variant="outline"
                className="w-full border-border text-white hover:bg-card"
              >
                View Calendar
              </Button>
            </Link>
          </div>
        </div>
      </div>
      <BottomNav />
    </>
  );
}