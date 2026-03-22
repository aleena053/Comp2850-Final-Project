import { RouterProvider } from 'react-router';
import { router } from './routes';

export default function App() {
  return (
    <div className="min-h-screen bg-background flex justify-center items-start">
      {/* Mobile app container - simulates a mobile device */}
      <div className="w-full max-w-[430px] min-h-screen bg-background relative shadow-[0_0_50px_rgba(0,245,212,0.1)] overflow-hidden">
        <RouterProvider router={router} />
      </div>
    </div>
  );
}